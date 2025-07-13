package com.letrasypapeles.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class BackendApplicationTests {

	@Test
	void contextLoads() {
		// This test ensures that the Spring context loads successfully
	}

	@Test
	void main_CallsSpringApplicationRun() {
		// Given
		String[] args = {"--spring.profiles.active=test"};

		// When & Then - Mock SpringApplication.run to verify it's called
		try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
			BackendApplication.main(args);

			mockedSpringApplication.verify(() ->
				SpringApplication.run(eq(BackendApplication.class), eq(args))
			);
		}
	}

	@Test
	void main_WithEmptyArgs_CallsSpringApplicationRun() {
		// Given
		String[] emptyArgs = {};

		// When & Then
		try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
			BackendApplication.main(emptyArgs);

			mockedSpringApplication.verify(() ->
				SpringApplication.run(eq(BackendApplication.class), eq(emptyArgs))
			);
		}
	}

	@Test
	void main_WithNullArgs_CallsSpringApplicationRun() {
		// Given
		String[] nullArgs = null;

		// When & Then
		try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
			BackendApplication.main(nullArgs);

			mockedSpringApplication.verify(() ->
				SpringApplication.run(eq(BackendApplication.class), eq(nullArgs))
			);
		}
	}

	@Test
	void main_WithMultipleArgs_CallsSpringApplicationRun() {
		// Given
		String[] multipleArgs = {"--server.port=8081", "--spring.profiles.active=dev", "--debug"};

		// When & Then
		try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
			BackendApplication.main(multipleArgs);

			mockedSpringApplication.verify(() ->
				SpringApplication.run(eq(BackendApplication.class), eq(multipleArgs))
			);
		}
	}

}
