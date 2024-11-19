package it.andrea.start;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import it.andrea.start.exception.BusinessException;
import it.andrea.start.exception.MappingToDtoException;
import it.andrea.start.exception.MappingToEntityException;
import it.andrea.start.exception.user.UserNotFoundException;
import it.andrea.start.exception.user.UserRoleAlreadyExistsException;
import it.andrea.start.exception.user.UserRoleNotFoundException;
import it.andrea.start.service.InitializeService;

@EnableWebMvc
@SpringBootApplication
public class StartProjectApplication extends SpringBootServletInitializer {

	private static final Logger LOG = LoggerFactory.getLogger(StartProjectApplication.class);

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {

		LOG.info("-----------------------------");
		LOG.info("StartApplication->configure");
		LOG.info("-----------------------------");

		final SpringApplicationBuilder applicationBuilder = application.sources(StartProjectApplication.class);
		ClassLoader loader = applicationBuilder.application().getClassLoader();

		LOG.info("loader : {}", loader.getName());

		return application.sources(StartProjectApplication.class);
	}

	public static void main(String[] args) throws UserNotFoundException, MappingToDtoException, MappingToEntityException, UserRoleNotFoundException, UserRoleAlreadyExistsException, BusinessException {

		LOG.info("-----------------------------");
		LOG.info("StartApplication->main");
		LOG.info("-----------------------------");

		ConfigurableApplicationContext context = SpringApplication.run(StartProjectApplication.class, args);
		try {
			context.getBean(InitializeService.class).executeStartOperation();
		} catch (BeansException e) {
			LOG.error(e.getMessage());
		}
	}

}