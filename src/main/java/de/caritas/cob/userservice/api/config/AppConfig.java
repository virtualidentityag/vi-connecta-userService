package de.caritas.cob.userservice.api.config;

import de.caritas.cob.userservice.api.admin.service.consultant.ConsultantReindexer;
import io.sentry.Sentry;
import io.sentry.SentryOptions;
import java.time.Clock;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;

/** Contains some general spring boot application configurations */
@Configuration
@ComponentScan(basePackages = {"de.caritas.cob.userservice"})
@PropertySources({@PropertySource("classpath:messages.properties")})
public class AppConfig implements ApplicationContextAware {

  @Value("${sentry.environment}")
  private String environment;

  @Value("${onlineberatung.sentry.dsn}")
  private String sentryDsn;

  @Value("${sentry.sample-rate:0.5}")
  private Double sampleRate;

  private ApplicationContext context;
  /**
   * Activate the messages.properties for validation messages
   *
   * @param messageSource
   * @return
   */
  @Bean
  public LocalValidatorFactoryBean validator(MessageSource messageSource) {
    LocalValidatorFactoryBean validatorFactoryBean = new LocalValidatorFactoryBean();
    validatorFactoryBean.setValidationMessageSource(messageSource);
    return validatorFactoryBean;
  }

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder.build();
  }

  @Bean
  public ConsultantReindexer consultantReindexer(EntityManagerFactory entityManagerFactory) {
    FullTextEntityManager manager =
        Search.getFullTextEntityManager(entityManagerFactory.createEntityManager());
    return new ConsultantReindexer(manager);
  }

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  @Primary
  public SentryOptions sentryOptions() {
    SentryOptions options = new SentryOptions();
    options.setEnvironment(environment);
    options.setDsn(sentryDsn);
    options.setTag("service", "MessageService");
    options.setRelease("2.0.0");
    options.setTracesSampleRate(sampleRate);
    options.setSendDefaultPii(false);
    return options;
  }

  @PostConstruct
  public void postConstructSentryOptions() {
    SentryOptions options = context.getBean(SentryOptions.class);
    options.setEnvironment(environment);
    options.setDsn(sentryDsn);
    options.setTag("service", "MessageService");
    options.setRelease("2.0.0");
    options.setTracesSampleRate(sampleRate);
    options.setSendDefaultPii(false);
    Sentry.init(options);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }
}
