package ly.iterative.example;

import ly.iterative.itly.*;
import ly.iterative.itly.jvm.*;
import ly.iterative.itly.iteratively.*;
import ly.iterative.itly.test.Schemas;
import ly.iterative.itly.test.events.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;

public class AppJava {
    public static void main(String[] args) {
        Environment environment = Environment.PRODUCTION;

        ValidationOptions validation = new ValidationOptions();

        Plugin iterativelyPlugin = new IterativelyPlugin(
            "itly-api-key",
            ly.iterative.itly.iteratively.IterativelyOptions.builder()
                .url("https://iterative.ly/about")
                .build()
        );

        Itly itly = new Itly();

        itly.load(
            Context.VALID_ONLY_REQUIRED_PROPS,
            new Options(
                environment,
                new ArrayList<>(Arrays.asList(
                    iterativelyPlugin,
                    new SchemaValidatorPlugin(Schemas.DEFAULT)
                )),
                false,
                validation
            ));
        String userId = "user-id";

        itly.identify(userId);

        final String output = iterativelyPlugin.id();
        System.out.println(output);
    }
}
