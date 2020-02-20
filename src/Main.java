public class Main {

    public static void main(String[] args) {
        Calculator calculator = new Calculator(System.in);
        calculator.setExitWord("/anotherExitWord");
        calculator.start();
        System.out.println("Bye!");
    }
}
