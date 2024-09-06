package com.hkust.isom5260;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.hkust.isom5260.service.USTStudentDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private DataSource dataSource;

	@Bean
	public UserDetailsService userDetailsService() {
		return new USTStudentDetailService();
	}

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService());
		authProvider.setPasswordEncoder(passwordEncoder());
		
		return authProvider;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.antMatchers("/users").authenticated()
			.anyRequest().permitAll()
			.and()
			.formLogin().loginPage("/login").usernameParameter("email").defaultSuccessUrl("/users")
			.failureHandler(authenticationFailureHandler()).permitAll()
			.and()
			.logout().logoutSuccessUrl("/").permitAll();
	}

	public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
		@Override
		public void onAuthenticationFailure(
				HttpServletRequest request,
				HttpServletResponse response,
				AuthenticationException exception)
				throws IOException, ServletException {
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			Map<String, Object> data = new HashMap<>();
			data.put("timestamp", Calendar.getInstance().getTime());
			data.put("exception", exception.getMessage());
			this.getRedirectStrategy().sendRedirect(request,response,"/login?error=" + HttpStatus.UNAUTHORIZED.value());
		}
	}

	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler() {
		return new CustomAuthenticationFailureHandler();
	}
	
}
