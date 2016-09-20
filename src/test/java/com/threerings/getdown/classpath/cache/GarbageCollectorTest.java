package com.threerings.getdown.classpath.cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.threerings.getdown.util.file.FileWalker;

/**
 * Validates that cache garbage is collected and deleted correctly.
 */
public class GarbageCollectorTest
{
    @Before
    public void setupFiles () throws IOException
    {
        _cachedFile = _folder.newFile("abc123.jar");
        _lastAccessedFile = _folder.newFile("abc123.jar" + ResourceCache.LAST_ACCESSED_FILE_SUFFIX);
    }

    @Test
    public void shouldDeleteCacheEntryIfRetentionPeriodIsReached ()
    {
        gcNow();

        assertFalse(_cachedFile.exists());
        assertFalse(_lastAccessedFile.exists());
    }

    @Test
    public void shouldDeleteCacheFolderIfFolderIsEmpty ()
    {
        gcNow();

        assertFalse(_folder.getRoot().exists());
    }

    private void gcNow() {
        GarbageCollector collector = new GarbageCollector(
                new FileWalker(_folder.getRoot()), -1, TimeUnit.MILLISECONDS);

        collector.collectGarbage();
    }

    @Test
    public void shouldKeepFilesInCacheIfRententionPeriodIsNotReached ()
    {
        GarbageCollector collector = new GarbageCollector(
                new FileWalker(_folder.getRoot()), 1, TimeUnit.DAYS);

        collector.collectGarbage();

        assertTrue(_cachedFile.exists());
        assertTrue(_lastAccessedFile.exists());
    }

    @Test
    public void shouldDeleteCachedFileIfLastAccessedFileIsMissing ()
    {
        assumeTrue(_lastAccessedFile.delete());

        gcNow();

        assertFalse(_cachedFile.exists());
    }

    @Test
    public void shouldDeleteLastAccessedFileIfCachedFileIsMissing ()
    {
        assumeTrue(_cachedFile.delete());

        gcNow();

        assertFalse(_lastAccessedFile.exists());
    }

    @Rule
    public TemporaryFolder _folder = new TemporaryFolder();

    private File _cachedFile;
    private File _lastAccessedFile;
}