/*
 * This file is part of Runes.
 *
 * Runes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Runes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Runes.  If not, see < http://www.gnu.org/licenses/ >.
 */
package com.wolvereness.runeslib;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.json.JsonNumber;
import javax.json.JsonString;
import javax.json.JsonValue;

import com.wolvereness.runeslib.Rune.Builder.Node;

/**
 * @author Wolfe
 */
public class Rune<T, E extends Throwable> {

	static class Base extends Builder implements Serializable {
		private static final long serialVersionUID = 1L;

		Base() { super((Object) null); }

		Base(final Base basis) {
			super(basis);
		}

		@Override
		public Builder drop(final int i) {
			return this;
		}
	}

	/**
	 * @author Wolfe
	 */
	public abstract static class Builder {
		class Node extends Builder implements Serializable {
			private static final long serialVersionUID = 1L;

			Node(final Node builder) {
				super(builder);
			}

			Node(final Object object) {
				super(object);
			}

			@Override
			public Builder drop(final int i) {
				if (i == 0)
					return this;
				return Builder.this.drop(i - 1);
			}
		}

		private static final Node[] EMPTY = new Node[0];
		final Object identifier;
		Node[] nodes = EMPTY;

		Builder(final Builder basis) {
			this.identifier = basis.identifier;
			final Node[] nodes = basis.nodes.clone();
			for (int i = 0, len = nodes.length; i < len; i++) {
				nodes[i] = new Node(nodes[i]);
			}
			this.nodes = nodes;
		}

		Builder(final Object identifier) {
			this.identifier = identifier;
		}

		Node add(final Object node) {
			Node[] nodes;
			System.arraycopy(nodes = this.nodes, 0, nodes = new Node[nodes.length + 1], 0, nodes.length - 1);
			return (this.nodes = nodes)[nodes.length - 1] = new Node(node);
		}

		public Rune<Boolean, AssertionError> booleanRune() {
			return booleanRune(AssertionError.class);
		}

		public Rune<Boolean, RuntimeException> booleanRune(final Boolean def) {
			return typedRune(BOOLEANS, def);
		}

		public <E extends Throwable> Rune<Boolean, E> booleanRune(final Class<E> clazz) {
			return typedRune(BOOLEANS, clazz);
		}

		public Rune<Double, AssertionError> doubleRune() {
			return doubleRune(AssertionError.class);
		}

		public <E extends Throwable> Rune<Double, E> doubleRune(final Class<E> clazz) {
			return typedRune(DOUBLES, clazz);
		}

		public Rune<Double, RuntimeException> doubleRune(final Double def) {
			return typedRune(DOUBLES, def);
		}

		/**
		 * Returns a view of the builder that was provided of the specified
		 * levels up.
		 * <p>
		 * <code>builder == {@link #in(Object) builder.in("node")}.drop(1)</code>
		 *
		 * @param i levels to drop
		 * @return the parent builder
		 */
		public abstract Builder drop(int i);

		/**
		 * Provides the builder with a node to attempt to grab.
		 *
		 * @param node the node to access
		 * @return a nested view of the builder
		 */
		public Builder in(final Object node) {
			return add(node);
		}

		public Rune<Long, AssertionError> longRune() {
			return longRune(AssertionError.class);
		}

		public <E extends Throwable> Rune<Long, E> longRune(final Class<E> clazz) {
			return typedRune(LONGS, clazz);
		}

		public Rune<Long, RuntimeException> longRune(final Long def) {
			return typedRune(LONGS, def);
		}

		/**
		 * Provides the builder with an alternative (to the last provided) node
		 * to attempt to grab.
		 *
		 * @param node the node to access
		 * @return a nested view of the builder
		 */
		public Builder or(final Object node) {
			return drop(1).add(node);
		}

		public Rune<String, AssertionError> stringRune() {
			return stringRune(AssertionError.class);
		}

		public <E extends Throwable> Rune<String, E> stringRune(final Class<E> clazz) {
			return typedRune(STRINGS, clazz);
		}

		public Rune<String, RuntimeException> stringRune(final String def) {
			return typedRune(STRINGS, def);
		}

		public <T> Rune<T, AssertionError> typedRune() {
			return typedRune(AssertionError.class);
		}

		public <T, E extends Throwable> Rune<T, E> typedRune(final Class<E> clazz) {
			return this.<T, E>typedRune(SELF, clazz);
		}

		public <T> Rune<T, RuntimeException> typedRune(final T def) {
			return typedRune(SELF, def);
		}

		public <T> Rune<T, AssertionError> typedRune(final Transformer<T> transformer) {
			return typedRune(transformer, AssertionError.class);
		}

		public <T, E extends Throwable> Rune<T, E> typedRune(final Transformer<T> transformer, final Class<E> exception) {
			return new Rune<T, E>((Base) this.drop(Integer.MAX_VALUE), transformer, null, exception);
		}

		public <T> Rune<T, RuntimeException> typedRune(final Transformer<T> transformer, final T def) {
			return new Rune<T, RuntimeException>((Base) drop(Integer.MAX_VALUE), transformer, def, null);
		}
	}

	/**
	 * @author Wolfe
	 * @param <T> type to transform
	 */
	public static interface Transformer<T> {
		/**
		 * @param object the object to transform, never null
		 * @return the value to transform to, never null
		 */
		T transform(Object object);
	}

	static final Transformer<Boolean> BOOLEANS = new Transformer<Boolean>()
		{
			public Boolean transform(final Object object) {
				if (object == null)
					return null;
				if (object instanceof Boolean)
					return (Boolean) object;
				if (JSON && object instanceof JsonValue) {
					if (object== JsonValue.FALSE)
						return Boolean.FALSE;
					if (object == JsonValue.TRUE)
						return Boolean.TRUE;
					if (object instanceof JsonString)
						return Boolean.valueOf(((JsonString) object).getString());
				} else if (object instanceof String) return Boolean.valueOf((String) object);
				throw new ClassCastException(object.getClass() + "(" + object + ") cannot be cast to Double");
			}
		};

	static final Transformer<Double> DOUBLES = new Transformer<Double>()
		{
			public Double transform(final Object object) {
				if (object == null)
					return null;
				if (object instanceof Double)
					return (Double) object;
				if (object instanceof Float)
					return ((Float) object).doubleValue();
				if (JSON && object instanceof JsonNumber)
					return ((JsonNumber) object).doubleValue();
				throw new ClassCastException(object.getClass() + "(" + object + ") cannot be cast to Double");
			}
		};

	static final boolean JSON;

	static final Transformer<Long> LONGS = new Transformer<Long>()
		{
			public Long transform(final Object object) {
				if (object == null)
					return null;
				if (object instanceof Long)
					return (Long) object;
				if (JSON && object instanceof JsonNumber)
					return ((JsonNumber) object).longValueExact();
				if (!(
						object instanceof Number
						&& (
							object instanceof Byte
							|| object instanceof Short
							|| object instanceof Integer
							)
						))
					throw new ClassCastException(object.getClass() + "(" + object + ") cannot be cast to Long");
				return ((Number) object).longValue();
			}
		};
	static final Object NULL = new Object();
	private static final boolean RECURSIVE = Boolean.valueOf(System.getProperty(Rune.class.getName() + ".RECURSION", "true"));
	private static final int REFS_INDEX = 0;
	private static final int REFS_MARKERS_INDEX = REFS_INDEX + 1;
	private static final int REFS_OBJECTS_INDEX = REFS_MARKERS_INDEX + 1;
	@SuppressWarnings("rawtypes")
	static final Transformer SELF = new Transformer<Object>()
		{
			public Object transform(final Object object) {
				if (JSON && object instanceof JsonValue) {
					switch (((JsonValue) object).getValueType()) {
					case NULL:
						return null;
					case FALSE:
						return false;
					case TRUE:
						return true;
					case STRING:
						return ((JsonString) object).getString();
					case NUMBER:
						return ((JsonNumber) object).doubleValue();
					case ARRAY:
					case OBJECT:
					default:
					}
				}
				return object;
			}
		};
	private static final ThreadLocal<Object[]> STACK = RECURSIVE ? null : new ThreadLocal<Object[]>();
	static final Transformer<String> STRINGS = new Transformer<String>()
		{
			public String transform(final Object object) {
				if (object == null)
					return null;
				if (JSON && object instanceof JsonString)
					return ((JsonString) object).getString();
				return object.toString();
			}
		};
	static {
		boolean json = true;
		try {
			javax.json.JsonValue.class.getName();
		} catch (final NoClassDefFoundError er) {
			json = false;
		}
		JSON = json;
	}

	/**
	 * @return a new builder
	 */
	public static Builder newBuilder() {
		return new Base();
	}

	static Object nullify(final Object object) {
		if (object == null)
			return NULL;
		return object;
	}

	private final Base basis;
	private final T def;
	private final Class<? extends E> exClass;
	private final Transformer<T> transformer;

	Rune(final Base basis, final Transformer<T> transformer, final T def, final Class<? extends E> exClass) {
		this.transformer = transformer;
		this.def = def;
		this.basis = new Base(basis);
		this.exClass = exClass;
	}

	/**
	 * Reads the appropriate value from the object, traversing maps and lists
	 * as necessary.
	 *
	 * @param object the object to read from
	 * @return the value read after being transformed, or the default
	 * @throws E the exception provided, if any
	 */
	public T read(final Object object) throws E {
		final Object value;
		if (RECURSIVE) {
			value = recurse(basis, object);
		} else {
			value = resolve(object);
		}

		if (value == NULL || (JSON && value == JsonValue.NULL))
			return transformer.transform(null);
		if (value == null) {
			if (exClass == null)
				return def;
			try {
				throw exClass.newInstance();
			} catch (final InstantiationException e) {
				throw new IllegalStateException(e);
			} catch (final IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}

		return transformer.transform(value);
	}

	private Object recurse(final Builder scope, final Object context) {
		final Builder.Node[] nodes = scope.nodes;
		final int length = nodes.length;

		if (length == 0)
			return nullify(context);

		if (context instanceof Map) {
			final Map<?, ?> map = (Map<?, ?>) context;
			for (int i = 0; i < length; i++) {
				final Node node = nodes[i];
				final Object identifier = node.identifier;

				if (!map.containsKey(identifier)) {
					continue;
				}

				final Object current = recurse(node, map.get(identifier));
				if (current != null)
					return current;
			}
		} else if (context instanceof List) {
			final List<?> list = (List<?>) context;
			for (int i = 0; i < length; i++) {
				final Node node = nodes[i];
				final Object identifier = node.identifier;
				if (!(identifier instanceof Integer)) {
					continue;
				}

				final int index = (Integer) identifier;
				if (index < 0 || index >= list.size()) {
					continue;
				}

				final Object current = recurse(node, list.get(index));
				if (current != null)
					return current;
			}
		}
		return null;
	}

	private Object resolve(final Object object) {
		Object[] stackContainer = STACK.get();
		if (stackContainer == null) {
			STACK.set(stackContainer = new Object[] { new Builder[10], new int[10], new Object[10] });
		}
		int depth = 0;
		Builder[] stackRefs = (Builder[]) stackContainer[REFS_INDEX];
		int[] stackMarkers = (int[]) stackContainer[REFS_MARKERS_INDEX];
		Object[] stackObjects = (Object[]) stackContainer[REFS_OBJECTS_INDEX];

		stackRefs[0] = basis;
		stackMarkers[0] = 0;
		stackObjects[0] = object;

		while (true) {
			final Object context = stackObjects[depth];
			final Builder.Node[] nodes = stackRefs[depth].nodes;
			final int length = nodes.length;

			if (length == 0) {
				if (depth == 0)
					return null;
				return nullify(context);
			}

			Object currentObject;
			Builder.Node node;
			int i;
			PUSH: {
				if (context instanceof Map) {
					final Map<?, ?> map = (Map<?, ?>) context;

					for (i = stackMarkers[depth]; i < length; i++) {
						node = nodes[i];
						final Object identifier = node.identifier;
						if (map.containsKey(identifier)) {
							currentObject = map.get(identifier);
							break PUSH;
						}
					}
				} else if (context instanceof List) {
					final List<?> list = (List<?>) context;

					for (i = stackMarkers[depth]; i < length; i++) {
						node = nodes[i];
						final Object identifier = node.identifier;

						if (!(identifier instanceof Integer)) {
							continue;
						}
						final int index = (Integer) identifier;

						if (index >= 0 && index < list.size()) {
							currentObject = list.get(index);
							break PUSH;
						}
					}
				}
				if (depth-- == 0)
					return null;
				continue;
			} // PUSH

			depth += 1;
			if (depth == stackRefs.length) {
				System.arraycopy(stackRefs, 0, stackContainer[REFS_INDEX] = stackRefs = new Builder[depth * 2], 0, depth);
				System.arraycopy(stackMarkers, 0, stackContainer[REFS_MARKERS_INDEX] = stackMarkers = new int[depth * 2], 0, depth);
				System.arraycopy(stackObjects, 0, stackContainer[REFS_OBJECTS_INDEX] = stackObjects = new Object[depth * 2], 0, depth);
			}

			stackRefs[depth] = node;
			stackMarkers[depth] = i + 1;
			stackObjects[depth] = currentObject;
		}
	}
}
