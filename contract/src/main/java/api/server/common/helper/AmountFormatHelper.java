package api.server.common.helper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

/**
 * 금액 포맷 유틸리티 클래스
 * 다양한 금액 포맷팅 기능을 제공합니다.
 */
@UtilityClass
@Slf4j
public class AmountFormatHelper {

    /**
     * 금액에 콤마 추가
     * @param amount 포맷팅할 금액 (정수 또는 Long)
     * @return 콤마가 추가된 문자열 금액 (예: 1,234,567)
     */
    public static String formatAmount(Object amount) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
        try {
            if (amount instanceof Number) {
                return numberFormat.format(amount);
            } else if (amount instanceof String) {
                String amountStr = (String) amount;
                Number parsedNumber = numberFormat.parse(amountStr);
                return numberFormat.format(parsedNumber);
            } else {
                log.error("Invalid amount type: Expected Number or String: {}", amount);
                return String.valueOf(amount);
            }
        } catch (ParseException e) {
            log.error("Invalid amount format: Unable to parse: {}", amount);
            return String.valueOf(amount);
        }
    }


    /**
     * 이율을 사용하여 이자 계산
     * @param principal 원금 (문자열 형식으로 입력)
     * @param rate 이율 (문자열 형식으로 퍼센트 값 입력, 예: "5.5")
     * @param period 기간 (문자열 형식으로 입력, 년 단위)
     * @return 계산된 이자 금액
     */
    public static String calculateInterest(String principal, String rate, String period) {
        try {
            long principalValue = Long.parseLong(principal.trim());
            double rateValue = Double.parseDouble(rate.trim());
            int periodValue = Integer.parseInt(period.trim());

            double interest = principalValue * (rateValue / 100) * periodValue;
            return formatAmount(Math.round(interest));
        } catch (NumberFormatException e) {
            log.error("Invalid interest value: [principal:{}] [rate:{}] [period:{}]", principal, rate, period);
            return "Invalid input for interest calculation";
        }
    }

    /**
     * 문자열 합산
     * @param values 합산할 문자열 배열
     * @return 문자열로 합산된 값
     */
    public static String sumStringValues(String... values) {
        try {
            long sum = 0;
            for (String value : values) {
                if (value != null && !value.trim().isEmpty()) {
                    sum += Long.parseLong(value.trim());
                }
            }
            return String.valueOf(sum);
        } catch (NumberFormatException e) {
            log.error("Invalid sum value: {}", (Object) values);
            return "Invalid input for summation";
        }
    }


    /**
     * 문자열 합산 및 사칙연산 처리
     * @param expression 연산식 문자열
     * @return 계산된 결과 문자열
     */
    public static String evaluateArithmeticExpression(String expression) {
        try {
            return String.valueOf(evaluateExpression(expression));
        } catch (Exception e) {
            return "Invalid input for calculation";
        }
    }


    /**
     * 사칙연산 표현식 평가
     * @param expression 연산식 문자열
     * @return 계산된 결과
     */
    private static long evaluateExpression(String expression) {
        Deque<Long> numbers = new ArrayDeque<>();
        Deque<Character> operators = new ArrayDeque<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isDigit(c)) {
                long num = 0;
                while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                    num = num * 10 + (expression.charAt(i) - '0');
                    i++;
                }
                i--;
                numbers.push(num);
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                    numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
                }
                operators.push(c);
            }
        }

        while (!operators.isEmpty()) {
            numbers.push(applyOperation(operators.pop(), numbers.pop(), numbers.pop()));
        }

        return numbers.pop();
    }


    /**
     * 연산자의 우선순위를 확인
     * @param op1 현재 연산자
     * @param op2 스택의 연산자
     * @return true면 스택의 연산자가 우선
     */
    private static boolean hasPrecedence(char op1, char op2) {
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    /**
     * 연산 수행
     * @param op 연산자
     * @param b 피연산자 1
     * @param a 피연산자 2
     * @return 연산 결과
     */
    private static long applyOperation(char op, long b, long a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Cannot divide by zero");
                }
                return a / b;
            default:
                throw new IllegalArgumentException("Invalid operator");
        }
    }

}