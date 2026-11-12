package me.rexsystems.rexChatGames.game;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstAnswerTest {

    @Test
    void secondCallerIsIgnored() {
        FirstAnswer flag = new FirstAnswer();
        assertTrue(flag.claim());
        assertFalse(flag.claim());
    }

    @Test
    void onlyOneCallerWinsUnderContention() throws Exception {
        FirstAnswer flag = new FirstAnswer();
        int callers = 200;
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(callers);
        AtomicInteger wins = new AtomicInteger();
        for (int i = 0; i < callers; i++) {
            pool.submit(() -> {
                start.await();
                if (flag.claim()) {
                    wins.incrementAndGet();
                }
                done.countDown();
                return null;
            });
        }
        start.countDown();
        assertTrue(done.await(10, java.util.concurrent.TimeUnit.SECONDS));
        pool.shutdown();
        assertEquals(1, wins.get());
    }
}
