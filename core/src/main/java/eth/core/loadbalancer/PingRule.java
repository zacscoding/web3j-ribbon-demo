package eth.core.loadbalancer;

import static java.util.Objects.requireNonNull;

import java.math.BigInteger;

/**
 * Alive or not rule for eth node
 */
public interface PingRule {

    /**
     * Determine to healthy if connected
     */
    boolean isConnectedOnly();

    /**
     * Determine to healthy until syncing state with in threshold
     */
    boolean isSyncing();

    /**
     * threshold for health status
     * e.g)
     *  1) threshold = 10, highestBlockNumber = 100, currentBlockNumber = 95 => healthy
     *  2) threshold = 10, highestBlockNumber = 100, currentBlockNumber = 80 => unhealthy
     */
    BigInteger getSyncingThreshold();

    /**
     * Determine to healthy if synchronized
     */
    boolean isSynchronized();

    final class ConnectedOnly implements PingRule {

        public static final ConnectedOnly INSTANCE = new ConnectedOnly();

        private ConnectedOnly() {
        }

        @Override
        public boolean isConnectedOnly() {
            return true;
        }

        @Override
        public boolean isSyncing() {
            return false;
        }

        @Override
        public BigInteger getSyncingThreshold() {
            return BigInteger.ZERO;
        }

        @Override
        public boolean isSynchronized() {
            return false;
        }
    }

    final class Syncing implements PingRule {

        private BigInteger threshold;

        public static Syncing of(BigInteger threshold) {
            requireNonNull(threshold, "threshold");
            if (threshold.compareTo(BigInteger.ZERO) <= 0) {
                throw new IllegalArgumentException("threshold must greater than 0");
            }

            return new Syncing(threshold);
        }

        private Syncing(BigInteger threshold) {
            this.threshold = threshold;
        }

        @Override
        public boolean isConnectedOnly() {
            return false;
        }

        @Override
        public boolean isSyncing() {
            return true;
        }

        @Override
        public BigInteger getSyncingThreshold() {
            return threshold;
        }

        @Override
        public boolean isSynchronized() {
            return false;
        }
    }

    final class Synchronized implements PingRule {

        public static final Synchronized INSTANCE = new Synchronized();

        private Synchronized() {
        }

        @Override
        public boolean isConnectedOnly() {
            return false;
        }

        @Override
        public boolean isSyncing() {
            return false;
        }

        @Override
        public BigInteger getSyncingThreshold() {
            return BigInteger.ZERO;
        }

        @Override
        public boolean isSynchronized() {
            return true;
        }
    }
}
