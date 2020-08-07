package ly.iterative.example;

import java.io.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppJavaTests {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(this.outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(this.originalOut);
    }

    @Test
    public void appJava_createAndRun_outputIterativelyPluginId() {
        AppJava app = new AppJava(); // MyClass is tested

        app.main(new String[]{"test-param"});

        String output = this.outContent.toString().trim();
        assertEquals(
            "iteratively", output,
            "should output the iteratively plugin id"
        );
    }
}

//class AppJavaTest extends Specification {
//
//    def 'Calling the entry point'() {
//
//        setup: 'Re-route standard out'
//        def buf = new ByteArrayOutputStream(1024)
//        System.out = new PrintStream(buf)
//
//        when: 'The entrypoint is executed'
//        Greeter.main('gradlephant')
//
//        then: 'The correct greeting is output'
//        buf.toString() == "Hello, Gradlephant\n".denormalize()
//    }
//}

//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.CsvSource;
//
//class AppJavaTests {
//
//    @Test
//    @DisplayName("1 + 1 = 2")
//    void addsTwoNumbers() {
////        Calculator calculator = new Calculator();
//        assertEquals(2, 1 + 1), "1 + 1 should equal 2");
//    }
//
////    @ParameterizedTest(name = "{0} + {1} = {2}")
////    @CsvSource({
////        "0,    1,   1",
////        "1,    2,   3",
////        "49,  51, 100",
////        "1,  100, 101"
////    })
////    void add(int first, int second, int expectedResult) {
////        Calculator calculator = new Calculator();
////        assertEquals(expectedResult, calculator.add(first, second),
////                () -> first + " + " + second + " should equal " + expectedResult);
////    }
//}
