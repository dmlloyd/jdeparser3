package com.example.spi;

import java.util.stream.Stream;
import java.util.Optional;
import java.util.Comparator;

/**
 * A sealed repository interface for entity persistence. Supports CRUD operations and querying.
 *
 * @since 2.0
 */
public sealed interface Repository<T, ID extends java.io.Serializable> extends Iterable permits AbstractRepository, ReadOnlyRepository {
    /**
     * Default page size for paginated queries.
     */
    int DEFAULT_PAGE_SIZE = 25;

    /**
     * Maximum batch size for bulk operations.
     */
    int MAX_BATCH_SIZE = 1000;

    /**
     * Finds an entity by its identifier.
     *
     * @return an optional containing the entity, or empty
     * @param id the entity identifier
     */
    Optional findById(ID id);

    /**
     * Saves the given entity.
     *
     * @return the saved entity
     * @param entity the entity to save
     */
    T save(T entity);

    /**
     * Deletes an entity by its identifier.
     *
     * @return {@code true} if the entity was deleted
     * @param id the entity identifier
     */
    boolean deleteById(ID id);

    /**
     * Returns all entities as a stream.
     *
     * @return a stream of all entities
     */
    Stream findAll();

    /**
     * Returns the total number of entities.
     *
     * @return the entity count
     */
    long count();

    /**
     * Checks whether an entity with the given ID exists.
     *
     * @return {@code true} if the entity exists
     * @param id the entity identifier
     */
    default boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    /**
     * Checks whether the repository is empty.
     *
     * @return {@code true} if the repository has no entities
     */
    default boolean isEmpty() {
        return count() == 0;
    }

    /**
     * Returns a comparator that uses natural ordering.
     *
     * @return a natural-order comparator
     */
    static <E extends Comparable> Comparator naturalOrder() {
        return Comparable::compareTo;
    }

    /**
     * Listener for repository change events.
     */
    public interface Listener<E> {
        /**
         * Called after an entity is saved.
         */
        void onSave(E entity);

        /**
         * Called after an entity is deleted.
         */
        void onDelete(Object id);
    }
}
