package org.omnaest.utils.repository.join;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.omnaest.utils.StreamUtils;
import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.element.lar.LeftAndRight;
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
    public BiElement<D1, D2> get(I id)
    {
        D1 element1 = this.elementRepositoryLeft.get(id);
        D2 element2 = this.elementRepositoryRight.get(id);
        return BiElement.of(element1, element2);
    }

    @Override
    public Stream<I> ids()
    {
        return StreamUtils.concat(this.elementRepositoryLeft.ids(), this.elementRepositoryRight.ids())
                          .distinct()
                          .map(id ->
                          {
                              I left = this.elementRepositoryLeft.containsId(id) ? id : null;
                              I right = this.elementRepositoryRight.containsId(id) ? id : null;
                              return new LeftAndRight<>(left, right);
                          })
                          .filter(this.mergedIdFilter)
                          .map(lar -> lar.hasLeft() ? lar.getLeft() : lar.getRight());
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
