import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        Calculator calculator = new Calculator(new FileInputStream(new File("test.txt")));
        calculator.setExitWord("/anotherExitWord");
        calculator.start();
        System.out.println("Bye!");
    }
}
