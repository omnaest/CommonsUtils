package org.omnaest.utils.repository.join;

import org.omnaest.utils.element.bi.BiElement;
import org.omnaest.utils.repository.ElementRepository;
import org.omnaest.utils.repository.ImmutableElementRepository;

public interface ElementRepositoryJoiner<I, DL, DR>
{
    public ImmutableElementRepository<I, BiElement<DL, DR>> inner();

    public ImmutableElementRepository<I, BiElement<DL, DR>> leftOuter();

    public ImmutableElementRepository<I, BiElement<DL, DR>> rightOuter();

    public ImmutableElementRepository<I, BiElement<DL, DR>> outer();

    public static <I, DL, DR> ElementRepositoryJoiner<I, DL, DR> of(ElementRepository<I, DL> elementRepositoryLeft,
                                                                    ElementRepository<I, DR> elementRepositoryRight)
    {
        return new ElementRepositoryJoinerImpl<>(elementRepositoryLeft, elementRepositoryRight);
    }

}
