package com.example.graphql.springbootgraphql.datafetcher;

import com.example.graphql.springbootgraphql.model.Contact;
import graphql.schema.DataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GraphqlDataFetcher {

    private final String REST_URL = "https://randomuser.me/api/?results=5000";

    @Autowired
    RestTemplate restTemplate;

    public DataFetcher<Contact> getContacts() {
        return dataFetchingEnvironment -> {
           return  restTemplate.exchange(REST_URL, HttpMethod.GET, null, new ParameterizedTypeReference<Contact>() {
            }).getBody();
        };
    }
}
