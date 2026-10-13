package com.db.migration;

import com.db.migration.model.legacy.UsuarioLegacy;
import com.db.migration.repository.legacy.UsuarioLegacyRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MigrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(MigrationApplication.class, args);

	}

}
