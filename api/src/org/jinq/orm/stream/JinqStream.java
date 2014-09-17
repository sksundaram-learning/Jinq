package org.jinq.orm.stream;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.jinq.tuples.Pair;
import org.jinq.tuples.Tuple3;
import org.jinq.tuples.Tuple4;
import org.jinq.tuples.Tuple5;

public interface JinqStream<T> extends Stream<T>
{
   @FunctionalInterface public static interface Where<U, E extends Exception> extends Serializable {
      public boolean where(U obj) throws E;
   }
   @FunctionalInterface public static interface WhereWithSource<U, E extends Exception> extends Serializable {
      public boolean where(U obj, InQueryStreamSource source) throws E;
   }
   public <E extends Exception> JinqStream<T> where(Where<T, E> test);
   public <E extends Exception> JinqStream<T> where(WhereWithSource<T, E> test);
   @FunctionalInterface public static interface Select<U, V> extends Serializable {
      public V select(U val);
   }
   @FunctionalInterface public static interface SelectWithSource<U, V> extends Serializable {
      public V select(U val, InQueryStreamSource source);
   }
   public <U> JinqStream<U> select(Select<T, U> select);
   public <U> JinqStream<U> select(SelectWithSource<T, U> select);
   // TODO: Joins are somewhat dangerous because certain types of joins that are
   // expressible here are NOT expressible in SQL. (Moving a join into
   // a from clause is only possible if the join does not access variables from
   // other things in the FROM clause *if* it ends up as a subquery. If we can 
   // express it as not a subquery, then it's ok.
   // TODO: Perhaps only providing a join(DBSet<U> other) is safer because
   // I think it will translate into valid SQL code, but it prevents people from
   // using navigational queries e.g. customers.join(customer -> customer.getPurchases);
   @FunctionalInterface public static interface Join<U, V> extends Serializable {
      public JinqStream<V> join(U val);
   }
   @FunctionalInterface public static interface JoinWithSource<U, V> extends Serializable {
      public JinqStream<V> join(U val, InQueryStreamSource source);
   }
   public <U> JinqStream<Pair<T, U>> join(Join<T,U> join);
   public <U> JinqStream<Pair<T, U>> join(JoinWithSource<T,U> join);
   public <U> JinqStream<Pair<T, U>> leftOuterJoin(Join<T,U> join);
   @FunctionalInterface public static interface AggregateGroup<W, U, V> extends Serializable {
      public V aggregateSelect(W key, JinqStream<U> val);
   }
   public <U, V> JinqStream<Pair<U, V>> group(Select<T, U> select, AggregateGroup<U, T, V> aggregate);
   /** @see #group(Select, AggregateGroup) */
   public <U, V, W> JinqStream<Tuple3<U, V, W>> group(Select<T, U> select, 
         AggregateGroup<U, T, V> aggregate1, AggregateGroup<U, T, W> aggregate2);
   /** @see #group(Select, AggregateGroup) */
   public <U, V, W, X> JinqStream<Tuple4<U, V, W, X>> group(Select<T, U> select, 
         AggregateGroup<U, T, V> aggregate1, AggregateGroup<U, T, W> aggregate2,
         AggregateGroup<U, T, X> aggregate3);
   /** @see #group(Select, AggregateGroup) */
   public <U, V, W, X, Y> JinqStream<Tuple5<U, V, W, X, Y>> group(Select<T, U> select, 
         AggregateGroup<U, T, V> aggregate1, AggregateGroup<U, T, W> aggregate2,
         AggregateGroup<U, T, X> aggregate3, AggregateGroup<U, T, Y> aggregate4);

   
   // TODO: This interface is a little iffy since the function can potentially return different number types
   // and things can't be checked until runtime, but Java type inferencing currently can't
   // disambiguate between different methods that take functions with different return types.
   // In most cases, this should be fine as long as programmers define V as something specific
   // like Integer or Double instead of something generic like Number.

   // These interfaces are used to define the lambdas used as parameters to various aggregation
   // operations.
   @FunctionalInterface public static interface CollectNumber<U, V extends Number & Comparable<V>> extends Serializable {
      public V aggregate(U val);
   }
   @FunctionalInterface public static interface CollectComparable<U, V extends Comparable<V>> extends Serializable {
      public V aggregate(U val);
   }
   public static interface CollectInteger<U> extends CollectNumber<U, Integer> {}
   public static interface CollectLong<U> extends CollectNumber<U, Long> {}
   public static interface CollectDouble<U> extends CollectNumber<U, Double> {}
   public static interface CollectBigDecimal<U> extends CollectNumber<U, BigDecimal> {}
   public static interface CollectBigInteger<U> extends CollectNumber<U, BigInteger> {}
   
   // Having separate sum() methods for different types is messy but due to problems
   // with Java's type inferencing and the fact that JPQL uses different return types
   // for a sum than the types being summed over, this is the only way to do sum
   // operations in a type-safe way.
   public Long sumInteger(CollectInteger<T> aggregate);
   public Long sumLong(CollectLong<T> aggregate);
   public Double sumDouble(CollectDouble<T> aggregate);
   public BigDecimal sumBigDecimal(CollectBigDecimal<T> aggregate);
   public BigInteger sumBigInteger(CollectBigInteger<T> aggregate);
   

   // TODO: It's more type-safe to have separate maxDouble(), maxDate(), etc. methods,
   // but it's too messy, so I'll provide this simpler max() method for now
   public <V extends Comparable<V>> V max(CollectComparable<T, V> aggregate);
   public <V extends Comparable<V>> V min(CollectComparable<T, V> aggregate);
   public <V extends Number & Comparable<V>> Double avg(CollectNumber<T, V> aggregate);
   
   @FunctionalInterface public static interface AggregateSelect<U, V> extends Serializable {
      public V aggregateSelect(JinqStream<U> val);
   }
//   public <U> U selectAggregates(AggregateSelect<T, U> aggregate);
//   public <U> U aggregate(AggregateSelect<T, U> aggregate1);
   public <U, V> Pair<U, V> aggregate(AggregateSelect<T, U> aggregate1,
         AggregateSelect<T, V> aggregate2);
   /**
    * @see #aggregate(AggregateSelect, AggregateSelect)
    */
   public <U, V, W> Tuple3<U, V, W> aggregate(AggregateSelect<T, U> aggregate1,
         AggregateSelect<T, V> aggregate2, AggregateSelect<T, W> aggregate3);
   /**
    * @see #aggregate(AggregateSelect, AggregateSelect)
    */
   public <U, V, W, X> Tuple4<U, V, W, X> aggregate(AggregateSelect<T, U> aggregate1,
         AggregateSelect<T, V> aggregate2, AggregateSelect<T, W> aggregate3,
         AggregateSelect<T, X> aggregate4);
   /**
    * @see #aggregate(AggregateSelect, AggregateSelect)
    */
   public <U, V, W, X, Y> Tuple5<U, V, W, X, Y> aggregate(AggregateSelect<T, U> aggregate1,
         AggregateSelect<T, V> aggregate2, AggregateSelect<T, W> aggregate3,
         AggregateSelect<T, X> aggregate4, AggregateSelect<T, Y> aggregate5);

   public <V extends Comparable<V>> JinqStream<T> sortedBy(CollectComparable<T, V> sortField);
   public <V extends Comparable<V>> JinqStream<T> sortedDescendingBy(CollectComparable<T, V> sortField);
   
   // Overriding the Stream API versions to return a JinqStream instead, so it's easier to chain them
   @Override public JinqStream<T> skip(long n);
   @Override public JinqStream<T> limit(long n);
   @Override public JinqStream<T> distinct();

   public T getOnlyValue();
   
   // TODO: Should toList() throw an exception?
   public List<T> toList();
   
   public String getDebugQueryString();
   
   /**
    * Used for recording an exception that occurred during processing
    * somewhere in the stream chain.
    *  
    * @param source lambda object that caused the exception (used so that
    *    if the same lambda causes multiple exceptions, only some of them 
    *    need to be recorded in order to avoid memory issues)
    * @param exception actual exception object
    */
   public void propagateException(Object source, Throwable exception);
   
   public Collection<Throwable> getExceptions();
   
   /**
    * Sets a hint on the stream for how the query should be executed
    * @param name
    * @param value
    * @return this
    */
   public JinqStream<T> setHint(String name, Object value);
   
   /**
    * Easy way to get a JinqStream from a collection. 
    */
   public static <U> JinqStream<U> from(Collection<U> collection)
   {
      return new NonQueryJinqStream<>(collection.stream());
   }

   /**
    * Creates a JinqStream containing a single object. 
    */
   public static <U> JinqStream<U> of(U value)
   {
      return new NonQueryJinqStream<>(Stream.of(value));
   }
}
