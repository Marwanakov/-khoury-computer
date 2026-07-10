package com.khourycomputer;

import org.springframework.boot.SpringApplication;

public class TestKhouryComputerApplication {

	public static void main(String[] args) {
		SpringApplication.from(KhouryComputerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
