//******************************************************************************
//
// File:    AntiprotonPlot.java
// Package: benchmarks.detinfer.pj.edu.rit.clu.antimatter
// Unit:    Class benchmarks.detinfer.pj.edu.rit.clu.antimatter.AntiprotonPlot
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

package benchmarks.detinfer.pj.edu.rit.clu.antimatter;

import benchmarks.detinfer.pj.edu.rit.numeric.ListXYSeries;

import benchmarks.detinfer.pj.edu.rit.numeric.plot.Dots;
import benchmarks.detinfer.pj.edu.rit.numeric.plot.Plot;
import benchmarks.detinfer.pj.edu.rit.numeric.plot.Strokes;

import benchmarks.detinfer.pj.edu.rit.vector.Vector2D;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class AntiprotonPlot is a main program that plots antiproton position data.
 * The antiproton positions are read from one or more {@linkplain
 * AntiprotonFile}s. These files are typically the output of one run of the
 * {@linkplain AntiprotonSeq} or {@linkplain AntiprotonClu} programs. The
 * AntiprotonPlot program displays a plot of the final antiproton positions (the
 * positions in the last snapshot). The program also displays the track of
 * antiproton index 0 (all the snapshots).
 * <P>
 * Usage: java benchmarks.detinfer.pj.edu.rit.clu.antimatter.AntiprotonPlot <I>file</I> [ <I>file</I>
 * . . . ]
 * <P>
 * Here is an example of an antiproton plot. It was generated by these commands:
 * <FONT SIZE="-1">
 * <PRE>
 * java benchmarks.detinfer.pj.edu.rit.clu.antimatter.AntiprotonSeq 142857 10 0.00001 1000 1000 20 plot.dat
 * java benchmarks.detinfer.pj.edu.rit.clu.antimatter.AntiprotonPlot plot.dat
 * </FONT>
 * </PRE>
 * <IMG SRC="doc-files/AntiprotonPlot.png">
 *
 * @author  Alan Kaminsky
 * @version 06-Feb-2008
 */
public class AntiprotonPlot
	{

// Prevent construction.

	private AntiprotonPlot()
		{
		}

// Global variables.

	// Array of antiproton positions.
	static Vector2D[] p;
	static int N;

	// Total momentum.
	static Vector2D totalMV = new Vector2D();

	// Data series for final antiproton positions.
	static ListXYSeries pFinal = new ListXYSeries();

	// Data series for antiproton 0 track.
	static ListXYSeries pTrack = new ListXYSeries();

// Main program.

	/**
	 * Main program.
	 */
	public static void main
		(String[] args)
		throws Exception
		{
		// Read first antiproton file.
		if (args.length < 1) usage();
		AntiprotonFile file = new AntiprotonFile();
		AntiprotonFile.Reader reader =
			file.prepareToRead
				(new BufferedInputStream
					(new FileInputStream (args[0])));
		N = file.getN();
		p = new Vector2D [N];
		for (int i = 0; i < N; ++ i)
			{
			p[i] = new Vector2D();
			}
		readFile (file, reader);

		// Read any other antiproton files.
		for (int i = 1; i < args.length; ++ i)
			{
			reader =
				file.prepareToRead
					(new BufferedInputStream
						(new FileInputStream (args[i])));
			readFile (file, reader);
			}

		// Set up data series with final antiproton positions.
		for (int i = 0; i < N; ++ i)
			{
			pFinal.add (p[i].x, p[i].y);
			}

		// Display plot.
		Plot plot = new Plot()
			.seriesDots (null)
			.seriesStroke (Strokes.solid (1.0))
			.xySeries (pTrack)
			.seriesDots (Dots.circle())
			.seriesStroke (null)
			.xySeries (pFinal)
			.leftMargin (24)
			.bottomMargin (24)
			.topMargin (12)
			.rightMargin (12)
			.majorGridLines (false);
		plot.getFrame().setVisible (true);
		}

// Hidden operations.

	/**
	 * Read antiproton position data from the given file.
	 *
	 * @param  file    Antiproton file.
	 * @param  reader  Antiproton file reader.
	 */
	private static void readFile
		(AntiprotonFile file,
		 AntiprotonFile.Reader reader)
		throws IOException
		{
		int snaps = file.getSnaps();
		int L = file.getL();
		int M = file.getM();
		boolean hasIndex0 = (L <= 0 && 0 < L+M);
		for (int s = 0; s < snaps; ++ s)
			{
			reader.readSnapshot (p, L, totalMV);
			if (hasIndex0) pTrack.add (p[0].x, p[0].y);
			}
		reader.close();
		}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java benchmarks.detinfer.pj.edu.rit.clu.antimatter.AntiprotonPlot <file> [<file> ...]");
		System.exit (1);
		}

	}