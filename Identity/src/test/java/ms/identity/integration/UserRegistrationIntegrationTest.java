package ms.identity.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import ms.identity.adapter.in.web.dtos.RegisterUserResponse;
import ms.identity.domain.model.UserStatus;

@AutoConfigureTestRestTemplate
class UserRegistrationIntegrationTest extends AbstractIntegrationTest {

	@Container
	static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

	@DynamicPropertySource
	static void configureProperties(final DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
		registry.add("spring.liquibase.enabled", () -> true);
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
	
	@Test
	void shouldShowDatabaseAndLiquibaseTables() {
	    System.out.println("Database: " +
	            jdbcTemplate.queryForObject(
	                    "SELECT current_database()", String.class));

	    jdbcTemplate.queryForList(
	            """
	            SELECT table_name
	            FROM information_schema.tables
	            WHERE table_schema = 'public'
	            ORDER BY table_name
	            """,
	            String.class
	    ).forEach(System.out::println);
	}

	//@Test
	void shouldRegisterUserAndPersistItInPostgres() {
		final String email = uniqueEmail();
		final String password = "StrongPassword123";

		final ResponseEntity<RegisterUserResponse> response = register(email, password);

		assertEquals(response.getStatusCode(), CREATED);
		assertNotNull(response.getBody());
		assertEquals(response.getBody().email(), email);
		assertEquals(response.getBody().status(), UserStatus.PENDING_VERIFICATION);

		final String storedHash = jdbcTemplate.queryForObject("SELECT password_hash FROM users WHERE email = ?",
				String.class, email);

		assertNotEquals(storedHash, password);
		assertTrue(passwordEncoder.matches(password, storedHash));
	}

	@Test
	void shouldReturnConflictWhenEmailAlreadyExists() {
		final String email = uniqueEmail();

		final ResponseEntity<RegisterUserResponse> firstResponse = register(email, "StrongPassword123");

		assertEquals(firstResponse.getStatusCode(), CREATED);

		final ResponseEntity<String> secondResponse = registerForError(email, "AnotherPassword123");

		assertEquals(secondResponse.getStatusCode(), CONFLICT);
	}

	@Test
	void shouldRejectInvalidRequest() {
		final ResponseEntity<String> response = registerForError("invalid-email", "short");

		assertEquals(response.getStatusCode(), BAD_REQUEST);
	}

	private ResponseEntity<RegisterUserResponse> register(final String email, final String password) {
		return restTemplate.postForEntity("/api/v1/auth/register", requestBody(email, password),
				RegisterUserResponse.class);
	}

	private ResponseEntity<String> registerForError(final String email, final String password) {
		return restTemplate.postForEntity("/api/v1/auth/register", requestBody(email, password), String.class);
	}

	private HttpEntity<Map<String, String>> requestBody(final String email, final String password) {

		final Map<String, String> body = Map.of("email", email, "password", password, "firstName", "Integration",
				"lastName", "Test");

		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		return new HttpEntity<>(body, headers);
	}

	private String uniqueEmail() {
		return "integration-" + UUID.randomUUID() + "@example.com";
	}
}
