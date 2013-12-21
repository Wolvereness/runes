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

/**
 * @author Wolfe
 */
public abstract class Rune<T, E extends Exception> {

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
		private Node[] nodes = EMPTY;

		Builder(final Builder basis) {
			this.identifier = basis.identifier;
			final Node[] nodes = basis.nodes.clone();
			for (int i = 0, len = nodes.length; i < len; i++) {
				nodes[i] = new Node(nodes[i]);
			}
		}

		Builder(final Object identifier) {
			this.identifier = identifier;
		}

		Node add(final Object node) {
			Node[] nodes;
			System.arraycopy(nodes = this.nodes, nodes.length, nodes = new Node[nodes.length + 1], 0, nodes.length - 1);
			return (this.nodes = nodes)[nodes.length - 1] = new Node(node);
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
	}

	/**
	 * @return a new builder
	 */
	public static Builder newBuilder() {
		return new Base();
	}

	private final Base basis;
	private final Class<? extends T> exClass;

	Rune(final Base basis, final Class<? extends T> exClass) {
		this.basis = new Base(basis);
		this.exClass = exClass;
	}

	public abstract T read(Object object) throws E;
}
