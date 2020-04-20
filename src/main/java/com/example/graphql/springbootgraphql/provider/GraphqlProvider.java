package com.example.graphql.springbootgraphql.provider;


import com.example.graphql.springbootgraphql.datafetcher.GraphqlDataFetcher;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.ExecutionStrategy;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.*;
import graphql.servlet.GraphQLServlet;
import graphql.servlet.SimpleGraphQLServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Component
public class GraphqlProvider {

    private GraphQL graphQL;

    @Autowired
    GraphqlDataFetcher dataFetcher;

    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        final Resource resource = new ClassPathResource("schema.graphql");
        String sdl = null;
        try {
            sdl = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        GraphQLSchema graphQLSchema = buildSchema(sdl);
        this.graphQL = GraphQL.newGraphQL(graphQLSchema).build();
        ExecutionStrategy executionStrategy = new AsyncExecutionStrategy();
        GraphQLServlet servlet = new SimpleGraphQLServlet(graphQLSchema, executionStrategy);
        ServletRegistrationBean bean = new ServletRegistrationBean(servlet, "/graphql");
        return bean;
    }

    private GraphQLSchema buildSchema(String sdl) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(sdl);
        RuntimeWiring runtimeWiring = buildWiring();
        SchemaGenerator schemaGenerator = new SchemaGenerator();
        return schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
    }
    private RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type(TypeRuntimeWiring.newTypeWiring("Query")
                        .dataFetcher("contacts", dataFetcher.getContacts()))
                .build();
    }
    @Bean
    public GraphQL graphQL() {
        return graphQL;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("*").allowedOrigins("http://localhost:*");
            }
        };
    }
}
