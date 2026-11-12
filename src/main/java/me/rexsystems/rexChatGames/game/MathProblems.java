package me.rexsystems.rexChatGames.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class MathProblems {

    public record Problem(int left, String operator, int right, int answer) {
        public String expression() {
            return left + " " + operator + " " + right;
        }
    }

    private enum Op {
        ADD("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/");

        private final String symbol;

        Op(String symbol) {
            this.symbol = symbol;
        }
    }

    private MathProblems() {
    }

    public static Problem generate(Random random, List<String> operations, int min, int max) {
        int lo = Math.min(min, max);
        int hi = Math.max(min, max);
        List<Op> ops = parse(operations);
        Op op = ops.get(random.nextInt(ops.size()));
        return switch (op) {
            case ADD -> arithmetic(random, lo, hi, Op.ADD);
            case SUBTRACT -> arithmetic(random, lo, hi, Op.SUBTRACT);
            case MULTIPLY -> arithmetic(random, lo, hi, Op.MULTIPLY);
            case DIVIDE -> divide(random, lo, hi);
        };
    }

    private static Problem arithmetic(Random random, int lo, int hi, Op op) {
        int left = pick(random, lo, hi);
        int right = pick(random, lo, hi);
        long raw = switch (op) {
            case ADD -> (long) left + right;
            case SUBTRACT -> (long) left - right;
            case MULTIPLY -> (long) left * (long) right;
            case DIVIDE -> 0L;
        };
        if (raw != (int) raw) {
            right = op == Op.MULTIPLY ? 1 : 0;
            raw = left;
        }
        return new Problem(left, op.symbol, right, (int) raw);
    }

    private static Problem divide(Random random, int lo, int hi) {
        int divisor = nonZero(random, lo, hi);
        int quotient = pick(random, lo, hi);
        long product = (long) divisor * (long) quotient;
        if (product != (int) product) {
            quotient = 1;
            product = divisor;
        }
        if (divisor == 0) {
            divisor = 1;
            product = quotient;
        }
        return new Problem((int) product, "/", divisor, quotient);
    }

    static int nonZero(Random random, int min, int max) {
        int lo = Math.min(min, max);
        int hi = Math.max(min, max);
        if (lo > 0 || hi < 0) {
            return pick(random, lo, hi);
        }
        long count = (long) hi - (long) lo;
        if (count <= 0) {
            return 1;
        }
        if (count > Integer.MAX_VALUE) {
            count = Integer.MAX_VALUE;
        }
        int roll = random.nextInt((int) count);
        long value = (long) lo + roll;
        if (value >= 0) {
            value++;
        }
        if (value == 0 || value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            return 1;
        }
        return (int) value;
    }

    private static int pick(Random random, int min, int max) {
        int lo = Math.min(min, max);
        int hi = Math.max(min, max);
        long span = (long) hi - (long) lo + 1L;
        if (span <= 1L) {
            return lo;
        }
        if (span > Integer.MAX_VALUE) {
            span = Integer.MAX_VALUE;
        }
        return (int) ((long) lo + random.nextInt((int) span));
    }

    private static List<Op> parse(List<String> operations) {
        List<Op> ops = new ArrayList<>();
        if (operations != null) {
            for (String raw : operations) {
                if (raw == null) {
                    continue;
                }
                Op op = switch (raw.trim().toLowerCase(Locale.ROOT)) {
                    case "add", "+" -> Op.ADD;
                    case "subtract", "-" -> Op.SUBTRACT;
                    case "multiply", "*" -> Op.MULTIPLY;
                    case "divide", "/" -> Op.DIVIDE;
                    default -> null;
                };
                if (op != null) {
                    ops.add(op);
                }
            }
        }
        if (ops.isEmpty()) {
            ops.add(Op.ADD);
        }
        return ops;
    }
}
