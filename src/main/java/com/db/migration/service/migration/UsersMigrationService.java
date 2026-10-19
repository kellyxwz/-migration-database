
package com.db.migration.service.migration;

import com.db.migration.model.legacy.UsuarioLegacy;
import com.db.migration.model.legacy.UserActivityDateLegacy;
import com.db.migration.model.target.MigrationIdMap;
import com.db.migration.model.target.Users;
import com.db.migration.repository.legacy.ChamadoLegacyRepository;
import com.db.migration.repository.legacy.InteracaoLegacyRepository;
import com.db.migration.repository.legacy.UsuarioLegacyRepository;
import com.db.migration.repository.target.MigrationIdMapRepository;
import com.db.migration.repository.target.UsersRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UsersMigrationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$"
    );

    private static final ZoneId LEGACY_ZONE =
            ZoneId.of("America/Sao_Paulo");

    private final UsersRepository usersRepository;
    private final MigrationIdMapRepository migrationIdMapRepository;
    private final UsuarioLegacyRepository usuarioLegacyRepository;
    private final ChamadoLegacyRepository chamadoLegacyRepository;
    private final InteracaoLegacyRepository interacaoLegacyRepository;

    public UsersMigrationService(
            UsersRepository usersRepository,
            MigrationIdMapRepository migrationIdMapRepository,
            UsuarioLegacyRepository usuarioLegacyRepository,
            ChamadoLegacyRepository chamadoLegacyRepository,
            InteracaoLegacyRepository interacaoLegacyRepository
    ) {
        this.usersRepository = usersRepository;
        this.migrationIdMapRepository = migrationIdMapRepository;
        this.usuarioLegacyRepository = usuarioLegacyRepository;
        this.chamadoLegacyRepository = chamadoLegacyRepository;
        this.interacaoLegacyRepository = interacaoLegacyRepository;
    }

    public void migrateUsers() {
        List<UsuarioLegacy> users = usuarioLegacyRepository.findAll();

        Map<Long, List<Instant>> activityDates = loadActivityDates();

        Map<String, List<UsuarioLegacy>> groups = users.stream()
                .filter(user -> !isTestAccount(user.getDsEmail()))
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

            Users newUser = buildUser(
                    group,
                    principal,
                    entry.getKey(),
                    activityDates
            );

            UUID newUserId = usersRepository.save(newUser);

            for (UsuarioLegacy legacyUser : group) {
                migrationIdMapRepository.save(new MigrationIdMap(
                        "users",
                        legacyUser.getCdUsuario(),
                        newUserId.toString()
                ));
            }
        }
    }

    private Users buildUser(
            List<UsuarioLegacy> group,
            UsuarioLegacy principal,
            String email,
            Map<Long, List<Instant>> activityDates
    ) {
        String fullName = normalizeName(principal.getName());
        String[] nameParts = fullName.split(" ", 2);

        Users user = new Users();
        user.setFirstName(nameParts[0]);
        user.setLastName(nameParts.length > 1 ? nameParts[1] : null);
        user.setEmail(email);
        user.setPhone(firstValidPhone(group));
        user.setDepartmentId(firstMappedDepartment(group));
        user.setRole(highestRole(group));
        user.setStatus(groupHasActiveValidEmail(group) ? "active" : "inactive");
        user.setPasswordHash(null);
        user.setMustResetPassword(true);
        user.setCreatedAt(calculateCreatedAt(group, activityDates));
        user.setUpdatedAt(Instant.now());
        user.setLegacyId(principal.getCdUsuario());

        return user;
    }

    private Map<Long, List<Instant>> loadActivityDates() {
        Map<Long, List<Instant>> dates = new HashMap<>();

        chamadoLegacyRepository.findUserActivityDates()
                .forEach(activity -> addActivityDate(dates, activity));

        interacaoLegacyRepository.findUserActivityDates()
                .forEach(activity -> addActivityDate(dates, activity));

        return dates;
    }

    private void addActivityDate(
            Map<Long, List<Instant>> dates,
            UserActivityDateLegacy activity
    ) {
        dates.computeIfAbsent(activity.userId(), id -> new ArrayList<>())
                .add(activity.activityAt());
    }

    private Instant calculateCreatedAt(
            List<UsuarioLegacy> group,
            Map<Long, List<Instant>> activityDates
    ) {
        List<Instant> dates = new ArrayList<>();

        for (UsuarioLegacy user : group) {
            Instant registrationDate = parseRegistrationDate(user.getDtCadastro());

            if (registrationDate != null) {
                dates.add(registrationDate);
            }

            List<Instant> userActivities =
                    activityDates.get(user.getCdUsuario().longValue());

            if (userActivities != null) {
                dates.addAll(userActivities);
            }
        }

        return dates.stream()
                .min(Comparator.naturalOrder())
                .orElseGet(() -> LocalDate.of(2010, 1, 4)
                        .atStartOfDay(LEGACY_ZONE)
                        .toInstant());
    }

    private Instant parseRegistrationDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (DateTimeFormatter formatter : List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        )) {
            try {
                return LocalDate.parse(value.trim(), formatter)
                        .atStartOfDay(LEGACY_ZONE)
                        .toInstant();
            } catch (DateTimeParseException ignored) {
                // Tenta o próximo formato previsto.
            }
        }

        return null;
    }

    private String normalizeEmail(String email, Long legacyId) {
        String normalized = email == null
                ? ""
                : email.trim().toLowerCase(Locale.ROOT);

        return normalized.isBlank() || !EMAIL_PATTERN.matcher(normalized).matches()
                ? "legado-" + legacyId + "@migracao.invalid"
                : normalized;
    }

    private boolean isTestAccount(String email) {
        return email != null
                && email.trim().toLowerCase(Locale.ROOT).endsWith("@teste.com");
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
            boolean particle = i > 0 && Set.of(
                    "da", "de", "do", "das", "dos", "e"
            ).contains(word);

            if (!particle) {
                word = word.substring(0, 1).toUpperCase(Locale.ROOT)
                        + word.substring(1);
            }

            if (i > 0) {
                result.append(' ');
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

        boolean validLength = digits.length() == 10 || digits.length() == 11;
        boolean validDdd = digits.length() >= 2
                && digits.charAt(0) != '0'
                && digits.charAt(1) != '0';
        boolean validMobile = digits.length() != 11
                || digits.charAt(2) == '9';

        return validLength && validDdd && validMobile
                ? "+55" + digits
                : null;
    }

    private String firstValidPhone(List<UsuarioLegacy> group) {
        return group.stream()
                .sorted(Comparator.comparing(UsuarioLegacy::getCdUsuario))
                .map(user -> normalizePhone(user.getNrTelefone()))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Long firstMappedDepartment(List<UsuarioLegacy> group) {
        return group.stream()
                .sorted(Comparator.comparing(UsuarioLegacy::getCdUsuario))
                .map(UsuarioLegacy::getCdSetor)
                .filter(Objects::nonNull)
                .map(id -> migrationIdMapRepository.findDepartmentNewId(
                        "department", id.longValue()
                ))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
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
            default -> 1;
        };
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

    private boolean groupHasActiveValidEmail(List<UsuarioLegacy> group) {
        return group.stream().anyMatch(user -> {
            String email = user.getDsEmail() == null
                    ? ""
                    : user.getDsEmail().trim().toLowerCase(Locale.ROOT);

            boolean validEmail = EMAIL_PATTERN.matcher(email).matches();
            boolean active = !"N".equalsIgnoreCase(
                    user.getFlAtivo() == null ? "" : user.getFlAtivo().trim()
            );

            return validEmail && active;
        });
    }
}
