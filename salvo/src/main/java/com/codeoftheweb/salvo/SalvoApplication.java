package com.codeoftheweb.salvo;

import com.codeoftheweb.salvo.models.*;
import com.codeoftheweb.salvo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.Date;


@SpringBootApplication

public class SalvoApplication extends SpringBootServletInitializer {
	public static void main(String[] args) {
		SpringApplication.run(SalvoApplication.class);
	}

	//encrypt the passwords before storing them with interface PasswordEncoder
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	@Bean
	public CommandLineRunner initData(
			PlayerRepository playerRepo,
			GameRepository gameRepo,
			GamePlayerRepository gamePlayerRepo,
			ShipRepository shipRepo,
			SalvoRepository salvoRepo,
			ScoreRepository scoreRepo) {
		return (args) -> {


			// Create Players
			Player player1 = new Player("j.bauer@ctu.gov", passwordEncoder().encode("24") );
			Player player2 = new Player("c.obrian@ctu.gov", passwordEncoder().encode("42"));
			Player player3 = new Player("kim_bauer@gmail.com",passwordEncoder().encode( "kb"));
			Player player4 = new Player("t.almeida@ctu.gov", passwordEncoder().encode("mole"));


			playerRepo.save(player1);
			playerRepo.save(player2);
			playerRepo.save(player3);
			playerRepo.save(player4);

			//Create Games, each game start 1 hour later than the one before.

			Date now = new Date();

			Game game1 = new Game();
			Game game2 = new Game();
			Game game3 = new Game();
			Game game4 = new Game();
			Game game5 = new Game();
			Game game6 = new Game();
			Game game7 = new Game();
			Game game8 = new Game();

			game1.setCreationDate(now);
			game2.setCreationDate(Date.from(now.toInstant().plusSeconds(3600)));
			game3.setCreationDate(Date.from(now.toInstant().plusSeconds(7200)));
			game4.setCreationDate(Date.from(now.toInstant().plusSeconds(10800)));
			game5.setCreationDate(Date.from(now.toInstant().plusSeconds(14400)));
			game6.setCreationDate(Date.from(now.toInstant().plusSeconds(18000)));
			game7.setCreationDate(Date.from(now.toInstant().plusSeconds(21600)));
			game8.setCreationDate(Date.from(now.toInstant().plusSeconds(25200)));


			gameRepo.save(game1);
			gameRepo.save(game2);
			gameRepo.save(game3);
			gameRepo.save(game4);
			gameRepo.save(game5);
			gameRepo.save(game6);
			gameRepo.save(game7);
			gameRepo.save(game8);


			//Create GamePlayers

			GamePlayer gamePlayer1 = new GamePlayer(game1, player1);
			GamePlayer gamePlayer2 = new GamePlayer(game1, player2);
			GamePlayer gamePlayer3 = new GamePlayer(game2, player1);
			GamePlayer gamePlayer4 = new GamePlayer(game2, player2);
			GamePlayer gamePlayer5 = new GamePlayer(game3, player2);
			GamePlayer gamePlayer6 = new GamePlayer(game3, player4);
			GamePlayer gamePlayer7 = new GamePlayer(game4, player2);
			GamePlayer gamePlayer8 = new GamePlayer(game4, player3);
			GamePlayer gamePlayer9 = new GamePlayer(game5, player1);
			GamePlayer gamePlayer10 = new GamePlayer(game5, player4);
			GamePlayer gamePlayer11 = new GamePlayer(game6, player3);
			GamePlayer gamePlayer12 = new GamePlayer(game7, player4);
			GamePlayer gamePlayer13 = new GamePlayer(game8, player4);
			GamePlayer gamePlayer14 = new GamePlayer(game8, player3);


			gamePlayerRepo.save(gamePlayer1);
			gamePlayerRepo.save(gamePlayer2);
			gamePlayerRepo.save(gamePlayer3);
			gamePlayerRepo.save(gamePlayer4);
			gamePlayerRepo.save(gamePlayer5);
			gamePlayerRepo.save(gamePlayer6);
			gamePlayerRepo.save(gamePlayer7);
			gamePlayerRepo.save(gamePlayer8);
			gamePlayerRepo.save(gamePlayer9);
			gamePlayerRepo.save(gamePlayer10);
			gamePlayerRepo.save(gamePlayer11);
			gamePlayerRepo.save(gamePlayer12);
			gamePlayerRepo.save(gamePlayer13);
			gamePlayerRepo.save(gamePlayer14);


			Ship ship1 = new Ship("destroyer", gamePlayer1, Arrays.asList("H2", "H3", "H4"));
			Ship ship2 = new Ship("submarine", gamePlayer1, Arrays.asList("E1", "F1", "G1"));
			Ship ship3 = new Ship("patrolBoat", gamePlayer1, Arrays.asList("B4", "B5"));
			Ship ship4 = new Ship("destroyer", gamePlayer2, Arrays.asList("B5", "C5", "D5"));
			Ship ship5 = new Ship("patrolBoat", gamePlayer2, Arrays.asList("F1", "F2"));

			Ship ship6 = new Ship("destroyer", gamePlayer3, Arrays.asList("B5", "C5", "D5"));
			Ship ship7 = new Ship("patrolBoat", gamePlayer3, Arrays.asList("C6", "C7"));
			Ship ship8 = new Ship("submarine", gamePlayer4, Arrays.asList("A2", "A3", "A4"));
			Ship ship9 = new Ship("patrolBoat", gamePlayer4, Arrays.asList("G6", "H6"));

			Ship ship10 = new Ship("destroyer", gamePlayer5, Arrays.asList("B5", "C5", "D5"));
			Ship ship11 = new Ship("patrolBoat", gamePlayer5, Arrays.asList("C6", "C7"));
			Ship ship12 = new Ship("submarine", gamePlayer6, Arrays.asList("A2", "A3", "A4"));
			Ship ship13 = new Ship("patrolBoat", gamePlayer6, Arrays.asList("G6", "H6"));

			Ship ship14 = new Ship("destroyer", gamePlayer7, Arrays.asList("B5", "C5", "D5"));
			Ship ship15 = new Ship("patrolBoat", gamePlayer7, Arrays.asList("C6", "C7"));
			Ship ship16 = new Ship("submarine", gamePlayer8, Arrays.asList("A2", "A3", "A4"));
			Ship ship17 = new Ship("patrolBoat", gamePlayer9, Arrays.asList("G6", "H6"));

			Ship ship18 = new Ship("destroyer", gamePlayer9, Arrays.asList("B5", "C5", "D5"));
			Ship ship19 = new Ship("patrolBoat", gamePlayer10, Arrays.asList("C6", "C7"));
			Ship ship20 = new Ship("submarine", gamePlayer10, Arrays.asList("A2", "A3", "A4"));
			Ship ship21 = new Ship("patrolBoat", gamePlayer11, Arrays.asList("G6", "H6"));
			Ship ship22 = new Ship("destroyer", gamePlayer11, Arrays.asList("B5", "C5", "D5"));
			Ship ship23 = new Ship("patrolBoat", gamePlayer12, Arrays.asList("C6", "C7"));
			Ship ship24 = new Ship("destroyer", gamePlayer12, Arrays.asList("B5", "C5", "D5"));
			Ship ship25 = new Ship("patrolBoat", gamePlayer13, Arrays.asList("G6", "H6"));
			Ship ship26 = new Ship("submarine", gamePlayer13, Arrays.asList("A2", "A3", "A4"));
			Ship ship27 = new Ship("patrolBoat", gamePlayer14, Arrays.asList("G6", "H6"));


			shipRepo.saveAll(Arrays.asList(ship1, ship2, ship3, ship4, ship5, ship6, ship7, ship8, ship9, ship10));
			shipRepo.saveAll(Arrays.asList(ship11, ship12, ship13, ship14, ship15, ship16, ship17, ship18, ship19, ship20));
			shipRepo.saveAll(Arrays.asList(ship21, ship22, ship23, ship24, ship25, ship26, ship27));

			Salvo salvo1 = new Salvo(1, gamePlayer1, Arrays.asList("B5", "C5", "F1"));
			Salvo salvo2 = new Salvo(1, gamePlayer2, Arrays.asList("B4", "B5", "B6"));
			Salvo salvo3 = new Salvo(2, gamePlayer1, Arrays.asList("F2", "D5"));
			Salvo salvo4 = new Salvo(2, gamePlayer2, Arrays.asList("E1", "H3", "A2"));
			Salvo salvo5 = new Salvo(1, gamePlayer3, Arrays.asList("A2", "A4", "G6"));
			Salvo salvo6 = new Salvo(1, gamePlayer4, Arrays.asList("B5", "D5", "C7"));
			Salvo salvo7 = new Salvo(2, gamePlayer3, Arrays.asList("A3", "H6"));
			Salvo salvo8 = new Salvo(2, gamePlayer4, Arrays.asList("C5", "C6"));
			Salvo salvo9 = new Salvo(1, gamePlayer5, Arrays.asList("H6", "A4", "G6"));
			Salvo salvo10 = new Salvo(1, gamePlayer6, Arrays.asList("H1", "H2", "H3"));
			Salvo salvo11 = new Salvo(2, gamePlayer5, Arrays.asList("A2", "A3", "D8"));
			Salvo salvo12 = new Salvo(2, gamePlayer6, Arrays.asList("E1", "F2", "G3"));
			Salvo salvo13 = new Salvo(1, gamePlayer7, Arrays.asList("A3", "A4", "F7"));
			Salvo salvo14 = new Salvo(1, gamePlayer8, Arrays.asList("B5", "C6", "H1"));
			Salvo salvo15 = new Salvo(2, gamePlayer7, Arrays.asList("A2", "G6", "H6"));
			Salvo salvo16 = new Salvo(2, gamePlayer8, Arrays.asList("C5", "C7", "D5"));


			salvoRepo.saveAll(Arrays.asList(salvo1, salvo2, salvo3, salvo4, salvo5, salvo6, salvo7, salvo8, salvo9, salvo10));
			salvoRepo.saveAll(Arrays.asList(salvo11, salvo12, salvo13, salvo14, salvo15, salvo16));

			Score score1 = new Score(game1, player1, 1.0);
			Score score2 = new Score(game1, player2, 0.0);
			Score score3 = new Score(game2, player1, 0.5);
			Score score4 = new Score(game2, player2, 0.5);
			Score score5 = new Score(game3, player2, 1.0);
			Score score6 = new Score(game3, player4, 0.0);
			Score score7 = new Score(game4, player2, 0.5);
			Score score8 = new Score(game4, player1, 0.5);

			scoreRepo.save(score1);
			scoreRepo.save(score2);
			scoreRepo.save(score3);
			scoreRepo.save(score4);
			scoreRepo.save(score5);
			scoreRepo.save(score6);
			scoreRepo.save(score7);
			scoreRepo.save(score8);

		};

	}
}

		@Configuration //tells Spring to create an instance of this class automatically
		class WebSecurityConfiguration extends GlobalAuthenticationConfigurerAdapter {

			@Autowired
			PlayerRepository playerRepository;


			@Override
			public void init(AuthenticationManagerBuilder auth) throws Exception {
				auth.userDetailsService(username -> {
					Player player = playerRepository.findByUserName(username);
					if (player != null) {
						return new User(player.getUserName(), player.getPassword(),
								AuthorityUtils.createAuthorityList("USER"));
					} else {
						throw new UsernameNotFoundException("Unknown user: " + username);
					}
				});
			}
		}
		//Enable URL Access to Authenticated Users
//the rules for what is public, how information is sent, and so on, is specified in the definition of the configure() method.
		@Configuration
		@EnableWebSecurity
		class WebSecurityConfig extends WebSecurityConfigurerAdapter {
			@Override
			protected void configure(HttpSecurity http) throws Exception {

				http.authorizeRequests()
						.antMatchers("/web/games.html", "/api/login", "/api/games", "/api/leaderBoard", "/web/styles/*", "/web/scripts/*", "/api/**","/api/players").permitAll()
						.antMatchers("/web/game.html*", "/api/game_view/**","/web/games.html*","/web/**").hasAuthority("USER")
						.antMatchers("/rest/**").permitAll()
						.anyRequest().denyAll();


				//login
				http.formLogin()
						.usernameParameter("username")
						.passwordParameter("password")
						.loginPage("/api/login");

				//logout
				http.logout().logoutUrl("/api/logout");

				// turn off checking for CSRF tokens
				http.csrf().disable();

				// if user is not authenticated, just send an authentication failure response
				http.exceptionHandling().authenticationEntryPoint((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

				// if login is successful, just clear the flags asking for authentication
				http.formLogin().successHandler((req, res, auth) -> clearAuthenticationAttributes(req));

				// if login fails, just send an authentication failure response
				http.formLogin().failureHandler((req, res, exc) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED));

				// if logout is successful, just send a success response
				http.logout().logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());

			}

			private void clearAuthenticationAttributes(HttpServletRequest request) {
				HttpSession session = request.getSession(false);
				if (session != null) {
					session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
				}
			}

		}

