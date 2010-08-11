package org.jbehave.core.io;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Finds stories by scanning file system. Stories can be either in the form of
 * embeddable class names or story paths.
 */
public class StoryFinder {

    private static final String JAVA = ".java";
    private final DirectoryScanner scanner;
    private final String classNameExtension;

    public StoryFinder() {
        this(JAVA);
    }

    public StoryFinder(String classNameExtension) {
        this(new DirectoryScanner(), classNameExtension);
    }

    public StoryFinder(DirectoryScanner scanner, String classNameExtension) {
        this.scanner = scanner;
        this.classNameExtension = classNameExtension;
    }

    /**
     * Finds java source paths from a base directory, allowing for
     * includes/excludes, and converts them to class names.
     * 
     * @param searchInDirectory
     *            the base directory path to search in
     * @param includes
     *            the List of include patterns, or <code>null</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>null</code> if none
     * @return A List of class names found
     */
    public List<String> findClassNames(String searchInDirectory, List<String> includes, List<String> excludes) {
        return classNames(normalise(scan(searchInDirectory, includes, excludes)));
    }

    /**
     * Finds paths from a base directory, allowing for includes/excludes. Paths
     * found are normalised by {@link StoryFinder#normalise(List<String>)}.
     * 
     * @param searchInDirectory
     *            the base directory path to search in
     * @param includes
     *            the List of include patterns, or <code>null</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>null</code> if none
     * @return A List of paths found
     */
    public List<String> findPaths(String searchInDirectory, List<String> includes, List<String> excludes) {
        return normalise(scan(searchInDirectory, includes, excludes));
    }

    /**
     * Finds paths from a base directory, allowing for includes/excludes. Paths
     * found are prefixed with specified path by {@link
     * StoryFinder#prefix(String, List<String>)} and normalised by {@link
     * StoryFinder#normalise(List<String>)}.
     * 
     * @param searchInDirectory
     *            the base directory path to search in
     * @param includes
     *            the List of include patterns, or <code>null</code> if none
     * @param excludes
     *            the List of exclude patterns, or <code>null</code> if none
     * @param prefixWith
     *            the root path prefixed to all paths found, or
     *            <code>null</code> if none
     * @return A List of paths found
     */
    public List<String> findPaths(String searchInDirectory, List<String> includes, List<String> excludes,
            String prefixWith) {
        return normalise(prefix(prefixWith, scan(searchInDirectory, includes, excludes)));
    }
    protected List<String> normalise(List<String> paths) {
        List<String> transformed = new ArrayList<String>(paths);
        CollectionUtils.transform(transformed, new Transformer() {
            public Object transform(Object input) {
                String path = (String) input;
                return path.replace('\\', '/');
            }
        });
        return transformed;
    }

    protected List<String> prefix(final String prefixWith, List<String> paths) {
        if (StringUtils.isBlank(prefixWith)) {
            return paths;
        }
        List<String> transformed = new ArrayList<String>(paths);
        CollectionUtils.transform(transformed, new Transformer() {
            public Object transform(Object input) {
                String path = (String) input;
                return prefixWith + path;
            }
        });
        return transformed;
    }

    protected List<String> classNames(List<String> paths) {
        List<String> trasformed = new ArrayList<String>(paths);
        CollectionUtils.transform(trasformed, new Transformer() {
            public Object transform(Object input) {
                String path = (String) input;
                if (!StringUtils.endsWithIgnoreCase(path, classNameExtension)) {
                    return input;
                }
                return StringUtils.removeEndIgnoreCase(path, classNameExtension).replace('/', '.');
            }
        });
        return trasformed;
    }

    protected List<String> scan(String basedir, List<String> includes, List<String> excludes) {
        if (!new File(basedir).exists()) {
            return new ArrayList<String>();
        }
        scanner.setBasedir(basedir);
        if (includes != null) {
            scanner.setIncludes(includes.toArray(new String[includes.size()]));
        }
        if (excludes != null) {
            scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
        }
        scanner.scan();
        return asList(scanner.getIncludedFiles());
    }

}