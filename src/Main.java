import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Deque<String> operations = new ArrayDeque<>();
        Deque<BigInteger> numbers = new ArrayDeque<>();
        Deque<String> postfix = new ArrayDeque<>();
        Map<String, BigInteger> variables = new HashMap<>();

        String input = sc.nextLine();
        while (!input.equals("/exit")) {
            operations.clear();
            numbers.clear();
            postfix.clear();
            if (input.matches("\\s*")) {
                input = sc.nextLine();
                continue;
            }
            if (input.equals("/help")) {
                System.out.println("2 -- 2 equals 2 - (-2) = 2 + 2 = 4\n" +
                        "2 ^ 4 + 4 * (5 + 6) = 60");
                continue;
            }
            if (input.matches("/.+")) {
                System.out.println("Unknown command");
                input = sc.nextLine();
                continue;
            }

            Matcher valueFinder = Pattern.compile("([a-zA-Z]+)\\s*=\\s*(-?\\d+)$").matcher(input);
            if (valueFinder.find()) {
                variables.put(valueFinder.group(1), new BigInteger(valueFinder.group(2)));
                input = sc.nextLine();
                continue;
            } else if (input.contains("=")) {
                valueFinder = Pattern.compile("([a-zA-Z]+)\\s*=\\s*([a-zA-Z]+)$").matcher(input);
                if (valueFinder.find()) {
                    if (variables.containsKey(valueFinder.group(2))) {
                        variables.put(valueFinder.group(1), variables.get(valueFinder.group(2)));
                    } else {
                        System.out.println("Unknown variable");
                    }
                    input = sc.nextLine();
                    continue;
                }

                String[] errorVar = input.split("\\s*=\\s*");
                if (errorVar.length > 2 || (errorVar.length == 2 && !errorVar[1].matches("\\d+"))) {
                    System.out.println("Invalid assignment");
                } else if (errorVar.length == 2 && !errorVar[0].matches("[a-zA-Z]+")) {
                    System.out.println("Invalid identifier");
                }
                input = sc.nextLine();
                continue;
            }

            String head = "";
            if (input.charAt(0) == '+' || input.charAt(0) == '-') {
                head = input.charAt(0) + "";
                input = input.substring(1);
            }

            input = input.replaceAll("\\(", " ( ")
                    .replaceAll("\\)", " ) ")
                    .replaceAll("\\++", " + ")
                    .replaceAll("\\*", " * ")
                    .replaceAll("/", " / ")
                    .replaceAll("\\^", " ^ ");

            StringBuilder builder = new StringBuilder();

            char prev = '0';
            for (char c : input.toCharArray()) {
                if (prev != '-' && c == '-' || prev == '-' && c != '-') {
                    builder.append(" ");
                }
                builder.append(c);
                prev = c;
            }

            input = head + builder.toString().replaceAll("\u00A0+", "");

            String error = "";
            for (String s : input.split("\\s+")) {

                if (s.equals("")) {
                    continue;
                }

                if (s.matches("\\++")) {
                    s = "+";
                } else if (s.matches("-+")) {
                    s = s.length() % 2 == 0 ? "+": "-";
                }

                if (!(variables.containsKey(s) || (s.charAt(0) == '+' || s.charAt(0) == '-') && variables.containsKey(s.substring(1))) && s.matches("(-?|\\+?)[a-zA-Z]+")) {
                    System.out.println("Unknown variable");
                    break;
                }

                if (variables.containsKey(s) || variables.containsKey(s.substring(1))) {
                    BigInteger value;
                    if (s.charAt(0) == '+') {
                        s = s.substring(1);
                        value = variables.get(s);
                    } else if (s.charAt(0) == '-') {
                        s = s.substring(1);
                        value = variables.get(s).negate();
                    } else {
                        value = variables.get(s);
                    }
                    s = "" + value;
                }

                if (s.matches("(-?|\\+?)\\d+")) {
                    if (s.charAt(0) == '+') {
                        s = s.substring(1);
                    }
                    postfix.offerLast(s);
                } else if ("(".equals(s)) {
                    operations.offerLast(s);
                } else if (")".equals(s)) {
                    boolean ok = false;
                    while (!operations.isEmpty()) {
                        String operation = operations.pollLast();
                        if ("(".equals(operation)) {
                            ok = true;
                            break;
                        }
                        postfix.offerLast(operation);
                    }
                    if (!ok) {
                        error = "Invalid expression";
                    }
                } else if (operations.isEmpty() || operations.peekLast().contains("(") || operationLevel(operations.peekLast()) < operationLevel(s)) {
                    operations.offerLast(s);
                } else if (operationLevel(operations.peekLast()) >= operationLevel(s)) {
                    String operation = operations.peekLast();
                    while (!operations.isEmpty() && operationLevel(operation) >= operationLevel(s)) {
                        postfix.offerLast(operations.pollLast());
                        operation = operations.peekLast();
                    }
                    operations.offerLast(s);
                }
            }
            while (!operations.isEmpty()) {
                String op = operations.pollLast();
                if (op.equals("(")) {
                    error = "Invalid expression";
                }
                postfix.offerLast(op);
            }
            while (!postfix.isEmpty()) {
                String p = postfix.pollFirst();
                if (p.matches("-?\\d+")) {
                    numbers.offerLast(new BigInteger(p));
                } else {
                    if (numbers.size() >= 2) {
                        BigInteger b = numbers.pollLast();
                        BigInteger a = numbers.pollLast();
                        numbers.offerLast(performOperation(p, a, b));
                    } else {
                        error = "Invalid expression";
                        break;
                    }
                }
            }
            if (numbers.size() == 1 && operations.isEmpty() && postfix.isEmpty() && "".equals(error)) {
                BigInteger result = numbers.pollLast();
                System.out.println(result);
            } else {
                System.out.println(error);
            }

            input = sc.nextLine();
        }
        System.out.println("Bye!");
    }

    public static int operationLevel(String c) {
        switch (c) {
            case "+" : return 1;
            case "-" : return 1;
            case "*" : return 2;
            case "/" : return 2;
            case "^" : return 3;
            default: return 0;
        }
    }

    public static BigInteger performOperation(String op, BigInteger a, BigInteger b) {
        switch (op) {
            case "+" : return a.add(b);
            case "-" : return a.subtract(b);
            case "*" : return a.multiply(b);
            case "/" : return a.divide(b);
            case "^" : return a.modPow(a, b);
            default: return BigInteger.ONE;
        }
    }
}
