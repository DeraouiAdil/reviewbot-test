package fr.uge.slice;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface Slice<E> {
	public Slice<E> subSlice(int from, int to);

	public int size();

	public E get(int index);

	public void replaceAll(UnaryOperator<E> function);

	@SafeVarargs
	public static <E> Slice<E> of(E... elements){
		Objects.requireNonNull(elements);
		return new SliceImpl<E>(elements, 0, elements.length);
	}
	
	default Slice<E> reversed() {
		return new Slice<E>() {
			@Override
			public E get(int index) {
				return Slice.this.get(size() - index - 1);
			}
// 1 2 3 4

// 4 3 2 1			
			@Override
			public int size() {
				return Slice.this.size();
			}

			@Override
			public String toString() {
				return IntStream.range(0, size()).mapToObj(e -> String.valueOf(get(e)))
						.collect(Collectors.joining(", ", "[", "]"));
			}

			public Slice<E> reversed() {
				return Slice.this;
			}

			@Override
			public void replaceAll(UnaryOperator<E> function) {
				Slice.this.replaceAll(function);
			}

			@Override
			public Slice<E> subSlice(int f, int t) {
				Objects.checkFromToIndex(f, t, size());
				var size = size();
				return Slice.this.subSlice(size - t,size - f).reversed();
			}
			// 1 2 3 4
			
		};
	}

	static <E> Slice<E> of(E[] elements, int from, int to) {
		Objects.requireNonNull(elements);
		Objects.checkFromToIndex(from, to, elements.length);
		return new SliceImpl<E>(elements, from, to);
	}

	final class SliceImpl<E> implements Slice<E> {
		private final E[] elements;
		private final int from;
		private final int to;

		private SliceImpl(E[] elements, int from, int to) {
			this.elements = elements;
			this.from = from;
			this.to = to;
			super();
		}

		@Override
		public String toString() {
			return Arrays.stream(elements, from, to).map(String::valueOf).collect(Collectors.joining(", ", "[", "]"));
		}

		@Override
		public int size() {
			return to - from;
		}

		@Override
		public E get(int index) {
			Objects.checkIndex(index, size());
			return elements[index + from];
		}

		@Override
		public void replaceAll(UnaryOperator<E> function) {
			Objects.requireNonNull(function);
			for (var i = from; i < to; i++) {
				elements[i] = function.apply(elements[i]);
			}
		}

		public Slice<E> subSlice(int f, int t) {
			Objects.checkFromToIndex(f, t, size());
			return new SliceImpl<E>(elements, f + this.from, this.from + t);
		}

	}
	//"foo", "bar", "baz", "whizz"
	//"bar", "baz", "whizz"
	//"baz", "whizz"
}
