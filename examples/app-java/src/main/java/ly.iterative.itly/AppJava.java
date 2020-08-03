package ly.iterative.itly;

import ly.iterative.itly.core.Itly;
import ly.iterative.itly.core.Options;
import ly.iterative.itly.test.Schemas;

import java.util.*;

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
            new ArrayList<Plugin>(List.<Plugin>of(
                iterativelyPlugin,
                new SchemaValidatorPlugin(
                    Schemas.DEFAULT_SCHEMA
                )
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
