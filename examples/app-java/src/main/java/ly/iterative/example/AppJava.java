package ly.iterative.example;

import ly.iterative.itly.*;
import ly.iterative.itly.jvm.*;
import ly.iterative.itly.iteratively.*;
import ly.iterative.itly.test.Schemas;

import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;

public class AppJava {
    public static void main(String[] args) {
        ValidationOptions validation = new ValidationOptions();

        Plugin iterativelyPlugin = new IterativelyPlugin(
            "itly-api-key",
            new IterativelyOptions("https://iterative.ly/about")
        );

        Itly itly = new Itly();

        itly.load(new Options(
            new Properties(Collections.emptyMap()),
            Environment.PRODUCTION,
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
