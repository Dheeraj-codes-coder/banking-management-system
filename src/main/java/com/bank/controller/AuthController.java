package com.bank.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

	@GetMapping("/login")
	public String login(Authentication authentication) {
		return "Login successful for " + authentication.getName();
	}
}
