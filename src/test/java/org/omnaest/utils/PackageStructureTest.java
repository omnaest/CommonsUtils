package org.omnaest.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.omnaest.utils.style.StyleProfile;
import org.omnaest.utils.style.sourcetext.SourceGuard;
import org.omnaest.utils.style.surface.CheckSurfaceGuard;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.EvaluationResult;

/**
 * Mechanically enforces this workspace's Java package structure guideline (see
 * {@code .claude/guidelines/java-package-structure.md}) against {@code CommonsUtils} via
 * {@code CommonsStyleSupport}'s LIBRARY profile (plan-208 Slice 3), following the same one-test-per-check shape
 * the workspace's other adopters use (e.g. {@code CommonsDuckDB}'s {@code PackageStructureTest}).
 *
 * <p>
 * <b>One deliberate difference from every other adopter: every check below runs against a LOCALLY-IMPORTED class
 * set, never {@link StyleProfile#mainClasses()}.</b> {@code CommonsUtils}' base package, {@code org.omnaest.utils},
 * is a SPLIT PACKAGE across {@code CommonsLangAndIO}, {@code CommonsJSONAndXML} and {@code CommonsUtils} itself.
 * {@code mainClasses()} does {@code new ClassFileImporter().importPackages(basePackage)}, which sweeps the WHOLE
 * CLASSPATH and yields classes from all three modules combined (~963) rather than this module's own (~99) - so
 * every check here is driven off {@link #CLASSES}, imported directly from this module's own
 * {@code target/classes} via {@code importPath(..)}, exactly as plan-208's Step 1 measurement was. Do NOT
 * "helpfully" switch this back to {@code PROFILE.mainClasses()} - that reintroduces the split-package
 * contamination this comment exists to name. Resolving the split-package limitation in {@code StyleProfile}
 * itself is out of scope for this adoption.
 *
 * <p>
 * <b>{@link #everyEnforcedCheckIsCalled()} (plan-225 Slice 7) is a coverage ratchet on this class itself.</b> It
 * uses {@link CheckSurfaceGuard} to prove, by ArchUnit bytecode analysis of this very test class, that every one
 * of {@code StyleProfile}'s and {@code SourceGuard}'s enforced (non-{@code @MeasurementOnly}) checks is actually
 * invoked and evaluated from a live {@code @Test} method here - not merely named in a comment or a javadoc block.
 * If {@code StyleProfile} gains a seventeenth enforced check, or a test method above is deleted, this test reds
 * and names the missing check, closing the drift plan-216 found and plan-225 mechanises. The
 * {@link #entryPointIsInterfaceOrUtilsFactory()} pin above counts as coverage because it still calls
 * {@code .evaluate(..)} from a live {@code @Test} method - the guard accepts both {@code ArchRule} terminal
 * forms.
 */
class PackageStructureTest
{

    private static final StyleProfile PROFILE = StyleProfile.library("org.omnaest.utils");

    /**
     * See this class's own javadoc for why this is imported directly from {@code target/classes} rather than via
     * {@link StyleProfile#mainClasses()}.
     */
    private static final JavaClasses  CLASSES = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                                                                       .importPath(Paths.get("target", "classes"));

    @Test
    void singleEntryPointAtContextRoot()
    {
        PROFILE.singleEntryPointAtContextRoot()
               .check(CLASSES);
    }

    /**
     * plan-208 AC-1's real-world proof, PINNED rather than omitted. {@code JsonFileElementCache} is a stateful
     * generic class sitting at the context root that is neither an interface nor a {@code *Utils} factory, and it
     * deliberately carries no {@code @ContextRoot} - a declaration buys count, never kind. The adopter idiom
     * otherwise permits opting out of a check by deleting its line; that is the wrong tool for a KNOWN standing
     * violation, because an omitted check is a guard switched off and nothing would tell you if a second
     * violation appeared. Assert the exact violation set instead: this test is green today, goes red if a second
     * violation appears anywhere at the context root, and goes red if {@code JsonFileElementCache} is ever fixed
     * or removed - at which point replace this pin with the ordinary
     * {@code PROFILE.entryPointIsInterfaceOrUtilsFactory().check(CLASSES)} call every other adopter uses.
     */
    @Test
    void entryPointIsInterfaceOrUtilsFactory()
    {
        EvaluationResult result = PROFILE.entryPointIsInterfaceOrUtilsFactory()
                                         .evaluate(CLASSES);
        List<String> details = result.getFailureReport()
                                     .getDetails();
        assertEquals(List.of(
                             "org.omnaest.utils.JsonFileElementCache is the entry point at its bounded-context root but is neither an "
                             + "interface nor a *Utils factory (guideline P2: a context root must be an interface, or, for a LIBRARY "
                             + "profile, a stateless *Utils factory) - turn it into an interface with a separate internal/ "
                             + "implementation, or rename it to a stateless *Utils factory"),
                     details);
    }

    @Test
    void noHorizontalLayerPackages()
    {
        PROFILE.noHorizontalLayerPackages()
               .check(CLASSES);
    }

    @Test
    void internalPackagesAreAccessedOnlyFromWithinTheirOwnSubtree()
    {
        PROFILE.internalPackagesAreAccessedOnlyFromWithinTheirOwnSubtree()
               .check(CLASSES);
    }

    @Test
    void repositoryTypesLiveInInternalRepository()
    {
        PROFILE.repositoryTypesLiveInInternalRepository()
               .check(CLASSES);
    }

    @Test
    void noDtoTypesOutsideInternal()
    {
        PROFILE.noDtoTypesOutsideInternal()
               .check(CLASSES);
    }

    @Test
    void internalSubPackagesAreRoleNamed()
    {
        PROFILE.internalSubPackagesAreRoleNamed()
               .check(CLASSES);
    }

    @Test
    void noInternalTypeOnAPublicApiSurface()
    {
        PROFILE.noInternalTypeOnAPublicApiSurface()
               .check(CLASSES);
    }

    @Test
    void boundedContextsAreDiscovered()
    {
        PROFILE.boundedContextsAreDiscovered()
               .check(CLASSES);
    }

    @Test
    void noContextDependsOnAnAdapter()
    {
        PROFILE.noContextDependsOnAnAdapter()
               .check(CLASSES);
    }

    @Test
    void adapterWireTypesLiveInTheirChannelDomain()
    {
        PROFILE.adapterWireTypesLiveInTheirChannelDomain()
               .check(CLASSES);
    }

    @Test
    void sharedTypesAreUsedByAtLeastTwoContexts()
    {
        PROFILE.sharedTypesAreUsedByAtLeastTwoContexts()
               .check(CLASSES);
    }

    @Test
    void utilsPackagesDoNotReachIntoDomain()
    {
        PROFILE.utilsPackagesDoNotReachIntoDomain()
               .check(CLASSES);
    }

    @Test
    void utilsPackagesDoNotDuplicateCommonsTypes()
    {
        PROFILE.utilsPackagesDoNotDuplicateCommonsTypes()
               .check(CLASSES);
    }

    @Test
    void noInternalReferencesFromOutsideTheirOwnSubtree()
    {
        SourceGuard.of()
                   .noInternalReferencesFromOutsideTheirOwnSubtree()
                   .verify();
    }

    @Test
    void testsMirrorTheirSubjectPackage()
    {
        SourceGuard.of()
                   .testsMirrorTheirSubjectPackage()
                   .verify();
    }

    @Test
    void everyEnforcedCheckIsCalled()
    {
        CheckSurfaceGuard.of(PackageStructureTest.class)
                         .verify();
    }

}
