package org.omnaest.utils.repository.join;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.omnaest.utils.EnumUtils;
import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.element.lar.LeftAndRight;
import org.omnaest.utils.optional.NullOptional;
import org.omnaest.utils.repository.ElementRepository;
import org.omnaest.utils.repository.ImmutableElementRepository;

public class ElementRepositoryJoinedView<I, D1, D2> implements ImmutableElementRepository<I, BiElement<D1, D2>>
{
    private ElementRepository<I, D1>      elementRepositoryLeft;
    private ElementRepository<I, D2>      elementRepositoryRight;
    private Predicate<LeftAndRight<I, I>> mergedIdFilter;

    public ElementRepositoryJoinedView(ElementRepository<I, D1> elementRepositoryLeft, ElementRepository<I, D2> elementRepositoryRight,
                                       Predicate<LeftAndRight<I, I>> mergedIdFilter)
    {
        super();
        this.elementRepositoryLeft = elementRepositoryLeft;
        this.elementRepositoryRight = elementRepositoryRight;
        this.mergedIdFilter = mergedIdFilter;
    }

    @Override
    public NullOptional<BiElement<D1, D2>> get(I id)
    {
        NullOptional<D1> element1 = this.elementRepositoryLeft.get(id);
        NullOptional<D2> element2 = this.elementRepositoryRight.get(id);
        return NullOptional.ofPresenceAndNullable(element1.isPresent() || element2.isPresent(),
                                                  () -> BiElement.of(element1.orElse(null), element2.orElse(null)));
    }

    @Override
    public Stream<I> ids(IdOrder idOrder)
    {
        return EnumUtils.decideOn(idOrder)
                        .ifEqualTo(IdOrder.ARBITRARY,
                                   () -> StreamUtils.concat(this.elementRepositoryLeft.ids(idOrder), this.elementRepositoryRight.ids(idOrder))
                                                    .distinct()
                                                    .map(id ->
                                                    {
                                                        I left = this.elementRepositoryLeft.containsId(id) ? id : null;
                                                        I right = this.elementRepositoryRight.containsId(id) ? id : null;
                                                        return new LeftAndRight<>(left, right);
                                                    })
                                                    .filter(this.mergedIdFilter)
                                                    .map(lar -> lar.hasLeft() ? lar.getLeft() : lar.getRight()))
                        .orElseThrow(() -> new IllegalArgumentException("Unsupported IdOrder value: " + idOrder));
    }

    @Override
    public long size()
    {
        return this.ids()
                   .count();
    }

    @Override
    public String toString()
    {
        return "ElementRepositoryJoiner [elementRepositoryLeft=" + this.elementRepositoryLeft + ", elementRepositoryRight=" + this.elementRepositoryRight + "]";
    }

}
