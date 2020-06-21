package org.omnaest.utils.repository.internal;

import java.util.function.Function;
import java.util.stream.Stream;

import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.JSONHelper.JsonStringConverter;
import org.omnaest.utils.MapperUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.functional.BidirectionalFunction;
import org.omnaest.utils.optional.NullOptional;
import org.omnaest.utils.repository.ElementAggregationRepository;
import org.omnaest.utils.repository.ElementAggregationRepository.LoggedEntry.State;
import org.omnaest.utils.repository.ElementRepository;
import org.omnaest.utils.repository.IndexElementRepository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ElementAggregationRepositoryImpl<I, D, M> implements ElementAggregationRepository<I, D, M>
{
    private ElementRepository<I, Long>             referenceRepository;
    private ElementRepository<I, Long>             removedReferenceRepository;
    private IndexElementRepository<LogEntry<D, M>> logRepository;

    public ElementAggregationRepositoryImpl(Factory factory, Class<I> idType, Class<D> dataType, Class<M> metaType)
    {
        super();
        this.referenceRepository = factory.newInstance("referenceRepository", idType, Long.class);
        this.removedReferenceRepository = factory.newInstance("removedReferenceRepository", idType, Long.class);
        JsonStringConverter<LogEntry<D, M>> converter = JSONHelper.converter(tf -> tf.constructParametricType(LogEntry.class, dataType, metaType));
        this.logRepository = IndexElementRepository.of(factory.newInstance("logRepository", Long.class, String.class)
                                                              .mapped(BidirectionalFunction.of(converter.deserializer(), converter.serializer())));
    }

    @Override
    public Stream<LoggedEntry<D, M>> getLog(I id)
    {
        Function<Long, Stream<LoggedEntry<D, M>>> referenceToEntryStreamMapper = reference -> StreamUtils.generate()
                                                                                                         .recursive(reference, this.logRepository::getValue,
                                                                                                                    LogEntry<D, M>::getPreviousReferenceId)
                                                                                                         .map(MapperUtils.<LogEntry<D, M>, LoggedEntry<D, M>>identity());
        return this.referenceRepository.get(id)
                                       .map(referenceToEntryStreamMapper)
                                       .orElseGet(() -> this.removedReferenceRepository.get(id)
                                                                                       .map(referenceToEntryStreamMapper)
                                                                                       .orElse(Stream.empty()));
    }

    @Override
    public ElementAggregationRepository<I, D, M> clear()
    {
        this.referenceRepository.clear();
        this.logRepository.clear();
        return this;
    }

    @Override
    public NullOptional<Entry<D, M>> get(I id)
    {
        return this.referenceRepository.get(id)
                                       .flatMap(resolvedReferenceId -> this.logRepository.get(resolvedReferenceId));
    }

    @Override
    public Stream<I> ids(IdOrder idOrder)
    {
        return this.referenceRepository.ids(idOrder);
    }

    @Override
    public long size()
    {
        return this.referenceRepository.size();
    }

    @Override
    public void put(I id, Entry<D, M> element)
    {
        NullOptional<Long> previousActiveReferenceId = this.referenceRepository.get(id);

        NullOptional<Long> previousReferenceId = previousActiveReferenceId.orElseGetOptional(() -> this.removedReferenceRepository.get(id));

        Long addedReferenceId = this.logRepository.add(new LogEntry<D, M>(previousReferenceId.orElse(null), element,
                                                                          previousActiveReferenceId.isPresent() ? State.UPDATED : State.CREATED));
        this.referenceRepository.put(id, addedReferenceId);
    }

    @Override
    public void remove(I id)
    {
        M meta = null;
        this.remove(id, meta);
    }

    @Override
    public void remove(I id, M meta)
    {
        this.referenceRepository.get(id)
                                .ifPresent(previousReferenceId ->
                                {
                                    Long referenceId = this.logRepository.add(new LogEntry<D, M>(previousReferenceId, meta, State.DELETED));
                                    this.removedReferenceRepository.put(id, referenceId);
                                    this.referenceRepository.remove(id);
                                });
    }

    @Override
    public I add(Entry<D, M> element)
    {
        return this.referenceRepository.add(this.logRepository.add(new LogEntry<D, M>(null, element, State.CREATED)));
    }

    protected static class LogEntry<D, M> implements LoggedEntry<D, M>
    {
        @JsonProperty
        private Long previousReferenceId;

        @JsonProperty
        private D data;

        @JsonProperty
        private M meta;

        @JsonProperty
        private State state;

        public LogEntry(Long previousReferenceId, Entry<D, M> entry, State state)
        {
            super();
            this.previousReferenceId = previousReferenceId;
            this.state = state;
            this.data = entry.get();
            this.meta = entry.getMeta();
        }

        protected LogEntry()
        {
            super();
        }

        public LogEntry(Long previousReferenceId, M meta, State state)
        {
            super();
            this.previousReferenceId = previousReferenceId;
            this.meta = meta;
            this.state = state;
        }

        @Override
        public State getState()
        {
            return this.state;
        }

        public D getData()
        {
            return this.data;
        }

        @JsonIgnore
        @Override
        public D get()
        {
            return this.getData();
        }

        @Override
        public M getMeta()
        {
            return this.meta;
        }

        public Long getPreviousReferenceId()
        {
            return this.previousReferenceId;
        }

        @Override
        public String toString()
        {
            return "LogEntry [previousReferenceId=" + this.previousReferenceId + ", data=" + this.data + ", meta=" + this.meta + ", state=" + this.state + "]";
        }

    }

}
