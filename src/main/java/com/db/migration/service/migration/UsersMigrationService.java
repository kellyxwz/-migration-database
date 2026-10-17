package com.db.migration.service.migration;

import com.db.migration.model.legacy.UsuarioLegacy;
import com.db.migration.model.target.MigrationIdMap;
import com.db.migration.model.target.Users;
import com.db.migration.repository.legacy.UsuarioLegacyRepository;
import com.db.migration.repository.target.MigrationIdMapRepository;
import com.db.migration.repository.target.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UsersMigrationService {

    private final UsersRepository usersRepository;
    private final MigrationIdMapRepository migrationIdMapRepository;
    private final UsuarioLegacyRepository usuarioLegacyRepository;

    public UsersMigrationService(UsersRepository usersRepository, MigrationIdMapRepository migrationIdMapRepository, UsuarioLegacyRepository usuarioLegacyRepository) {
        this.usersRepository = usersRepository;
        this.migrationIdMapRepository = migrationIdMapRepository;
        this.usuarioLegacyRepository = usuarioLegacyRepository;
    }

    public void migrateUsers() {

        List<UsuarioLegacy> legacyUsers = usuarioLegacyRepository.findAll();


        List<UsuarioLegacy> eligibleUsers = legacyUsers.stream()
                .filter(user -> !isTestAccount(user.getDsEmail()))
                .toList();

        Map<String, List<UsuarioLegacy>> groups = eligibleUsers.stream()
                .collect(Collectors.groupingBy(
                        user -> normalizeEmail(
                                user.getDsEmail(),
                                user.getCdUsuario()
                        ),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<String, List<UsuarioLegacy>> entry : groups.entrySet()) {
            List<UsuarioLegacy> group = entry.getValue();

            group.sort(Comparator.comparing(UsuarioLegacy::getCdUsuario));
            UsuarioLegacy principal = group.get(0);

            Users newUser = buildNewUser(group, principal, entry.getKey());

            UUID newUserId = usersRepository.save(newUser);

            for (UsuarioLegacy legacyUser : group) {

                MigrationIdMap migration= new MigrationIdMap(
                        "users",
                        legacyUser.getCdUsuario(),
                        String.valueOf(newUserId)
                );

                migrationIdMapRepository.save(
                        migration
                );
            }
        }
    }

    private Users buildNewUser(
            List<UsuarioLegacy> group,
            UsuarioLegacy principal,
            String normalizedEmail
    ) {
        String fullName = normalizeName(principal.getName());

        String[] nameParts = fullName.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : null;

        String role = highestRole(group);
        String status = groupHasActiveValidEmail(group)
                ? "active"
                : "inactive";

        String phone = firstValidPhone(group);
        Long departmentId = firstMappedDepartment(group);

        Users user = new Users();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(normalizedEmail);
        user.setPhone(phone);
        user.setDepartmentId(departmentId);
        user.setRole(role);
        user.setStatus(status);
        user.setPasswordHash(null);
        user.setMustResetPassword(true);

        // TODO: calcular createdAt pela regra de cadastro + atividades migradas.
        // Não inventar a data aqui; por enquanto ela fica sem valor.
        user.setCreatedAt(null);


        user.setUpdatedAt(java.time.Instant.now());

        // Identifica o registro principal do grupo.
        user.setLegacyId(principal.getCdUsuario());

        return user;
    }

    private String highestRole(List<UsuarioLegacy> group) {
        return group.stream()
                .map(user -> normalizeRole(user.getTpPerfil()))
                .max(Comparator.comparingInt(this::rolePriority))
                .orElse("requester");
    }

    private int rolePriority(String role) {
        return switch (role) {
            case "admin" -> 3;
            case "agent" -> 2;
            default -> 1; // requester
        };
    }

    private boolean groupHasActiveValidEmail(List<UsuarioLegacy> group) {
        return group.stream().anyMatch(user -> {
            String email = user.getDsEmail() == null
                    ? ""
                    : user.getDsEmail().trim().toLowerCase(Locale.ROOT);

            boolean validEmail = !email.isBlank()
                    && EMAIL_PATTERN.matcher(email).matches();

            boolean active = !"N".equalsIgnoreCase(
                    user.getFlAtivo() == null
                            ? ""
                            : user.getFlAtivo().trim()
            );

            return validEmail && active;
        });
    }

    private String firstValidPhone(List<UsuarioLegacy> group) {
        return group.stream()
                .map(user -> normalizePhone(user.getNrTelefone()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Long firstMappedDepartment(List<UsuarioLegacy> group) {

        for (UsuarioLegacy user : group) {
            Long legacyDepartmentId = user.getCdUsuario();

            if (legacyDepartmentId == null) {
                continue;
            }

            Optional<Long> newDepartmentId =
                    migrationIdMapRepository.findDepartmentNewId(
                            "departments",
                            legacyDepartmentId
                    );

            if (newDepartmentId.isPresent()) {
                return newDepartmentId.get();
            }
        }

        return null;
    }


    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$"
    );


    private String normalizeEmail(String email, Long legacyId) {
        String normalized = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);

        if (normalized.isBlank()
                || !EMAIL_PATTERN.matcher(normalized).matches()) {
            return "legado-" + legacyId + "@migracao.invalid";
        }

        return normalized;
    }

    private boolean isTestAccount(String email) {
        return email != null
                && email.trim().toLowerCase(Locale.ROOT)
                .endsWith("@teste.com");
    }


    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return "Usuário";
        }

        String[] words = name.trim().replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT).split(" ");

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];

            boolean particle = i > 0 && (
                    word.equals("da") || word.equals("de")
                            || word.equals("do") || word.equals("das")
                            || word.equals("dos") || word.equals("e")
            );

            if (!particle && !word.isEmpty()) {
                word = word.substring(0, 1).toUpperCase(Locale.ROOT)
                        + word.substring(1);
            }

            if (i > 0) {
                result.append(" ");
            }

            result.append(word);
        }

        return result.toString();
    }


    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String digits = phone.replaceAll("\\D", "");

        if ((digits.length() == 12 || digits.length() == 13)
                && digits.startsWith("55")) {
            digits = digits.substring(2);
        } else if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }

        boolean validLength = digits.length() == 10
                || digits.length() == 11;

        boolean validDdd = digits.length() >= 2
                && digits.charAt(0) != '0'
                && digits.charAt(1) != '0';

        boolean validMobile = digits.length() != 11
                || digits.charAt(2) == '9';

        if (!validLength || !validDdd || !validMobile) {
            return null;
        }

        return "+55" + digits;
    }

    private String normalizeRole(String profile) {
        if (profile == null) {
            return "requester";
        }

        return switch (profile.trim().toUpperCase(Locale.ROOT)) {
            case "A" -> "admin";
            case "T" -> "agent";
            case "U" -> "requester";
            default -> "requester";
        };
    }


    private String normalizeStatus(String activeFlag, String email) {
        String normalizedEmail = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);

        boolean invalidEmail = normalizedEmail.isBlank()
                || !EMAIL_PATTERN.matcher(normalizedEmail).matches();

        if (invalidEmail || "N".equalsIgnoreCase(
                activeFlag == null ? "" : activeFlag.trim())) {
            return "inactive";
        }

        return "active";
    }



    private String migratedPasswordHash() {
        return null;
    }

    private boolean mustResetPassword() {
        return true;
    }

}
