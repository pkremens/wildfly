package org.jboss.as.ejb3.remote;


import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction;
import org.jboss.ejb.client.XidTransactionID;
import org.jboss.tm.ImportedTransaction;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

public final class ImportedTransactionCache {
  private static Field arjunacore;
  private static Field jts;

  static {
    AccessController.doPrivileged(new PrivilegedAction<Void>() {
      @Override
      public Void run() {
        try {
          // I don't see any workaround currently other than modifying arjuna method visibility
          arjunacore = com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple.class.getDeclaredField("_theTransaction");
          jts = com.arjuna.ats.internal.jta.transaction.jts.TransactionImple.class.getDeclaredField("_theTransaction");
          arjunacore.setAccessible(true);
          jts.setAccessible(true);
        } catch (NoSuchFieldException e) {
          throw new RuntimeException(e);
        }
        return null;
      }
    });
  }

  private static long calculateWhenTimesOut(ImportedTransaction tx) {
    if (tx instanceof com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple) {
      try {
        Object key = arjunacore.get(tx);
        return TransactionReaper.transactionReaper().getRemainingTimeoutMills(key) + System.currentTimeMillis();
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    } else {
      try {
        com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction atomicTx = (AtomicTransaction) jts.get(tx);
        Object key = atomicTx.getControlWrapper();
        return TransactionReaper.transactionReaper().getRemainingTimeoutMills(key) + System.currentTimeMillis();
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private final NavigableSet<TxWrapper> timeouts = new TreeSet<TxWrapper>();
  private final Map<XidTransactionID, TxWrapper> transactions = new HashMap<XidTransactionID, TxWrapper>();


  public synchronized int size() {
    return transactions.size();
  }

  public synchronized boolean containsKey(Object key) {
    if (key == null) throw new NullPointerException();
    cleanUp();
    return transactions.containsKey(key);
  }

  private void cleanUp() {
    if (timeouts.isEmpty()) return;
    final long now = System.currentTimeMillis();
    TxWrapper eldest = timeouts.first();
    while (!timeouts.isEmpty() && eldest.timesOutAt < now) {
      timeouts.remove(eldest);
      transactions.remove(eldest.id);
      eldest = timeouts.first();
    }
  }

  public synchronized ImportedTransaction get(Object key) {
    if (key == null) throw new NullPointerException();
    cleanUp();
    TxWrapper wrapper = transactions.get(key);
    return unwrap(wrapper);
  }

  private static ImportedTransaction unwrap(TxWrapper wrapper) {
    return wrapper == null ? null : wrapper.transaction;
  }

  public synchronized ImportedTransaction put(XidTransactionID key, ImportedTransaction value) {
    if (key == null) throw new NullPointerException();
    TxWrapper newTx = new TxWrapper(key, value);
    TxWrapper wrapper = transactions.put(key, newTx);
    timeouts.add(newTx);
    cleanUp();
    return unwrap(wrapper);
  }

  public synchronized ImportedTransaction remove(Object key) {
    if (key == null) throw new NullPointerException();
    TxWrapper wrapper = transactions.remove(key);
    if (wrapper != null) timeouts.remove(wrapper);
    cleanUp();
    return unwrap(wrapper);
  }

  public synchronized void clear() {
    transactions.clear();
    timeouts.clear();
  }

  public synchronized ImportedTransaction putIfAbsent(XidTransactionID key, ImportedTransaction value) {
    if (key == null) throw new NullPointerException();
    ImportedTransaction prev = get(key);
    if (prev != null) return prev;
    put(key, value);
    return null;
  }

  private static final class TxWrapper implements Comparable<TxWrapper> {
    final XidTransactionID id;
    final ImportedTransaction transaction;
    final long timesOutAt;

    private TxWrapper(XidTransactionID id, ImportedTransaction transaction) {
      this.id = id;
      this.transaction = transaction;
      this.timesOutAt = calculateWhenTimesOut(transaction);
    }

    @Override
    public int compareTo(TxWrapper that) {
      return (int) (this.timesOutAt - that.timesOutAt);
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof TxWrapper)) return false;
      TxWrapper that = (TxWrapper) o;
      return this.transaction.equals(that.transaction);
    }

    @Override
    public int hashCode() {
      return transaction.hashCode();
    }
  }
}
