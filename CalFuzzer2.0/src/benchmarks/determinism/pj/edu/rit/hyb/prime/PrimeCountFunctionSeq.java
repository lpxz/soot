//******************************************************************************
//
// File:    PrimeCountFunctionSeq.java
// Package: benchmarks.determinism.pj.edu.rit.hyb.prime
// Unit:    Class benchmarks.determinism.pj.edu.rit.hyb.prime.PrimeCountFunctionSeq
//
// This Java source file is copyright (C) 2008 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java Library ("PJ"). PJ is free
// software; you can redistribute it and/or modify it under the terms of the GNU
// General Public License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// PJ is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package benchmarks.determinism.pj.edu.rit.hyb.prime;

import benchmarks.determinism.pj.edu.rit.pj.Comm;

import java.io.File;

/**
 * Class PrimeCountFunctionSeq is a sequential program that calculates the prime
 * counting function &pi;(<I>x</I>). &pi;(<I>x</I>) is the number of primes less
 * than or equal to <I>x</I>. The program uses a list of 32-bit primes stored in
 * a file. The prime file must be generated by the {@linkplain Prime32File}
 * program. To find the primes, the program calculates a series of sieves. Each
 * sieve consists of one million numbers.
 * <P>
 * Usage: java benchmarks.determinism.pj.edu.rit.hyb.prime.PrimeCountFunctionSeq <I>x</I>
 * <I>primefile</I>
 * <BR><I>x</I> = Argument of prime counting function, 0 &lt;= <I>x</I> &lt;=
 * 2<SUP>63</SUP>-1
 * <BR><I>primefile</I> = Prime file name
 * <P>
 * The computation is performed sequentially in a single processor. The program
 * measures the total running time. This establishes a benchmark for measuring
 * the running time on a parallel processor.
 *
 * @author  Alan Kaminsky
 * @version 05-Jun-2008
 */
public class PrimeCountFunctionSeq
	{

// Prevent construction.

	private PrimeCountFunctionSeq()
		{
		}

// Shared global variables.

	// Sieve in one-million-number chunks.
	static final int CHUNK = 1000000;

	// Command line arguments.
	static long x;
	static File primefile;

// Main program.

	/**
	 * Main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		// Start timing.
		long t1 = System.currentTimeMillis();

		// World communicator.
		Comm.init (args);

		// Parse command line arguments.
		if (args.length != 2) usage();
		x = Long.parseLong (args[0]);
		if (x < 0) usage();
		primefile = new File (args[1]);

		// Set up sieve.
		Sieve sieve = new Sieve (0, CHUNK);

		// Set up list of 32-bit primes.
		Prime32List primeList = new Prime32List (primefile);

		// For counting primes. Initially 1 to count prime number 2.
		long primeCount = 1;

		// Do all chunks.
		for (long lb = 0; lb >= 0 && lb <= x; lb += CHUNK)
			{
			// Get an iterator for the odd primes.
			LongIterator iter = primeList.iterator();

			// Sieve the chunk.
			sieve.lb (lb);
			sieve.initialize();
			sieve.sieveOut (iter);

			// Count primes <= x left in the chunk.
			iter = sieve.iterator();
			long p;
			while ((p = iter.next()) != 0 && p <= x) ++ primeCount;
			}

		// Stop timing.
		long t2 = System.currentTimeMillis();

		// Print the answer.
		System.out.println ("pi("+x+") = "+primeCount);
		System.out.println ((t2-t1)+" msec");
		}

// Hidden operations.

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java benchmarks.determinism.pj.edu.rit.hyb.prime.PrimeCountFunctionSeq <x> <primefile>");
		System.err.println ("<x> = Argument of prime counting function, 0 <= <x> <= 2^63-1");
		System.err.println ("<primefile> = Prime file name");
		System.exit (1);
		}

	}
