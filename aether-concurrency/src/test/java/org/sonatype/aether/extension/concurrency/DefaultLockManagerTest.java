package org.sonatype.aether.extension.concurrency;

/*******************************************************************************
 * Copyright (c) 2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.sonatype.aether.extension.concurrency.LockManager.Lock;

import edu.umd.cs.mtc.MultithreadedTestCase;
import edu.umd.cs.mtc.TestFramework;

@SuppressWarnings( "unused" )
public class DefaultLockManagerTest
{
    private DefaultLockManager manager;

    @Before
    public void setup()
    {
        manager = new DefaultLockManager();
    }

    @Test
    public void testCanonicalFile()
        throws Throwable
    {
        final File a = new File( "target", "a/b" );
        final File b = new File( "target", "a/./b" );
        
        TestFramework.runOnce( new MultithreadedTestCase()
        {
            public void thread1()
                throws LockingException
            {
                Lock lock = manager.writeLock( a );
                lock.lock();
                waitForTick( 3 );
                lock.unlock();
            }

            public void thread2()
                throws LockingException
            {
                waitForTick( 1 );
                Lock lock = manager.writeLock( b );
                lock.lock();
                assertTick( 3 );
            }
        } );
    }

    @Test
    public void testWriteBlocksRead()
        throws Throwable
    {
        final File a = new File( "target", "a/b" );
        final File b = new File( "target", "a/b" );

        TestFramework.runOnce( new MultithreadedTestCase()
        {
            public void thread1()
                throws LockingException
            {
                Lock lock = manager.writeLock( a );
                lock.lock();
                waitForTick( 2 );
                lock.unlock();
            }

            public void thread2()
                throws LockingException
            {
                waitForTick( 1 );
                Lock lock = manager.readLock( b );
                lock.lock();
                assertTick( 2 );
                lock.unlock();
            }
        } );
    }

    @Test
    public void testReadDoesNotBlockRead()
        throws Throwable
    {
        final File a = new File( "target", "a/b" );
        final File b = new File( "target", "a/b" );

        TestFramework.runOnce( new MultithreadedTestCase()
        {
            public void thread1()
                throws LockingException
            {
                Lock lock = manager.readLock( a );
                lock.lock();
                waitForTick( 2 );
                lock.unlock();
            }

            public void thread2()
                throws LockingException
            {
                waitForTick( 1 );
                Lock lock = manager.readLock( b );
                lock.lock();
                assertTick( 1 );
                lock.unlock();
            }
        } );
    }

    @Test
    public void testReadBlocksWrite()
        throws Throwable
    {
        final File a = new File( "target", "a/b" );
        final File b = new File( "target", "a/b" );

        TestFramework.runOnce( new MultithreadedTestCase()
        {
            public void thread1()
                throws LockingException
            {
                Lock lock = manager.readLock( a );
                lock.lock();
                waitForTick( 2 );
                lock.unlock();
            }

            public void thread2()
                throws LockingException
            {
                waitForTick( 1 );
                Lock lock = manager.writeLock( b );
                lock.lock();
                assertTick( 2 );
                lock.unlock();
            }
        } );
    }

    @Test
    public void testWriteBlocksWrite()
        throws Throwable
    {
        final File a = new File( "target", "a/b" );
        final File b = new File( "target", "a/b" );

        TestFramework.runOnce( new MultithreadedTestCase()
        {
            public void thread1()
                throws LockingException
            {
                Lock lock = manager.writeLock( a );
                lock.lock();
                waitForTick( 2 );
                lock.unlock();
            }

            public void thread2()
                throws LockingException
            {
                waitForTick( 1 );
                Lock lock = manager.writeLock( b );
                lock.lock();
                assertTick( 2 );
                lock.unlock();
            }
        } );
    }

}
