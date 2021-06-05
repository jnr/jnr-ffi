package jnr.ffi;

import java.util.List;
import java.util.Objects;

/**
 * Data class containing information about a loaded native library.
 *
 * A list of all currently loaded libraries can be queried using {@link Runtime#getLoadedLibraries()} which will
 * return a list of {@link LoadedLibraryData}s.
 */
// TODO: 30-May-2021 @basshelal: Better docs everywhere here!
public class LoadedLibraryData {

    private final List<String> libraryNames;
    private final List<String> searchPaths;
    private final List<String> successfulPaths;

    public LoadedLibraryData(List<String> libraryNames, List<String> searchPaths, List<String> successfulPaths) {
        this.libraryNames = libraryNames;
        this.searchPaths = searchPaths;
        this.successfulPaths = successfulPaths;
    }

    public List<String> getLibraryNames() {
        return libraryNames;
    }

    public List<String> getSearchPaths() {
        return searchPaths;
    }

    public List<String> getSuccessfulPaths() {
        return successfulPaths;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LoadedLibraryData)) return false;
        LoadedLibraryData that = (LoadedLibraryData) o;
        return Objects.equals(libraryNames, that.libraryNames) &&
                Objects.equals(searchPaths, that.searchPaths) &&
                Objects.equals(successfulPaths, that.successfulPaths);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraryNames, searchPaths, successfulPaths);
    }

    @Override
    public String toString() {
        return "LoadedLibraryData {" +
                "libraryNames=" + libraryNames +
                ", searchPaths=" + searchPaths +
                ", successfulPaths=" + successfulPaths +
                '}';
    }
}
