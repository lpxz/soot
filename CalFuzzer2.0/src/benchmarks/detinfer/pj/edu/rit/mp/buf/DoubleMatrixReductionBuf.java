//******************************************************************************
//
// File:    DoubleMatrixReductionBuf.java
// Package: benchmarks.detinfer.pj.edu.rit.mp.buf
// Unit:    Class benchmarks.detinfer.pj.edu.rit.mp.buf.DoubleMatrixReductionBuf
//
// This Java source file is copyright (C) 2007 by Alan Kaminsky. All rights
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

package benchmarks.detinfer.pj.edu.rit.mp.buf;

import benchmarks.detinfer.pj.edu.rit.mp.Buf;
import benchmarks.detinfer.pj.edu.rit.mp.DoubleBuf;

import benchmarks.detinfer.pj.edu.rit.pj.reduction.DoubleOp;
import benchmarks.detinfer.pj.edu.rit.pj.reduction.Op;
import benchmarks.detinfer.pj.edu.rit.pj.reduction.ReduceArrays;

import benchmarks.detinfer.pj.edu.rit.util.Range;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;

/**
 * Class DoubleMatrixReductionBuf provides a reduction buffer for class
 * {@linkplain DoubleMatrixBuf}.
 *
 * @author  Alan Kaminsky
 * @version 25-Oct-2007
 */
class DoubleMatrixReductionBuf
	extends DoubleMatrixBuf
	{

// Hidden data members.

	DoubleOp myOp;

// Exported constructors.

	/**
	 * Construct a new double matrix reduction buffer. It is assumed that the
	 * rows and columns of <TT>theMatrix</TT> are allocated and that each row of
	 * <TT>theMatrix</TT> has the same number of columns.
	 *
	 * @param  theMatrix    Matrix.
	 * @param  theRowRange  Range of rows to include.
	 * @param  theColRange  Range of columns to include.
	 * @param  op           Binary operation.
	 *
	 * @exception  NullPointerException
	 *     (unchecked exception) Thrown if <TT>op</TT> is null.
	 */
	public DoubleMatrixReductionBuf
		(double[][] theMatrix,
		 Range theRowRange,
		 Range theColRange,
		 DoubleOp op)
		{
		super (theMatrix, theRowRange, theColRange);
		if (op == null)
			{
			throw new NullPointerException
				("DoubleMatrixReductionBuf(): op is null");
			}
		myOp = op;
		}

// Exported operations.

	/**
	 * Store the given item in this buffer.
	 * <P>
	 * The <TT>put()</TT> method must not block the calling thread; if it does,
	 * all message I/O in MP will be blocked.
	 *
	 * @param  i     Item index in the range 0 .. <TT>length()</TT>-1.
	 * @param  item  Item to be stored at index <TT>i</TT>.
	 */
	public void put
		(int i,
		 double item)
		{
		int row = (i / myColCount) * myRowStride + myLowerRow;
		int col = (i % myColCount) * myColStride + myLowerCol;
		myMatrix[row][col] = myOp.op (myMatrix[row][col], item);
		}

	/**
	 * Copy items from the given buffer to this buffer. The number of items
	 * copied is this buffer's length or <TT>theSrc</TT>'s length, whichever is
	 * smaller. If <TT>theSrc</TT> is this buffer, the <TT>copy()</TT> method
	 * does nothing.
	 *
	 * @param  theSrc  Source of items to copy into this buffer.
	 *
	 * @exception  ClassCastException
	 *     (unchecked exception) Thrown if <TT>theSrc</TT>'s item data type is
	 *     not the same as this buffer's item data type.
	 */
	public void copy
		(Buf theSrc)
		{
		if (theSrc == this)
			{
			}
		else if (theSrc instanceof DoubleMatrixBuf)
			{
			DoubleMatrixBuf src = (DoubleMatrixBuf) theSrc;
			ReduceArrays.reduce
				(src.myMatrix, src.myRowRange, src.myColRange,
				 this.myMatrix, this.myRowRange, this.myColRange,
				 myOp);
			}
		else
			{
			DoubleBuf.defaultCopy ((DoubleBuf) theSrc, this);
			}
		}

	/**
	 * Create a buffer for performing parallel reduction using the given binary
	 * operation. The results of the reduction are placed into this buffer.
	 *
	 * @param  op  Binary operation.
	 *
	 * @exception  ClassCastException
	 *     (unchecked exception) Thrown if this buffer's element data type and
	 *     the given binary operation's argument data type are not the same.
	 */
	public Buf getReductionBuf
		(Op op)
		{
		throw new UnsupportedOperationException();
		}

// Hidden operations.

	/**
	 * Receive as many items as possible from the given byte buffer to this
	 * buffer.
	 * <P>
	 * The <TT>receiveItems()</TT> method must not block the calling thread; if
	 * it does, all message I/O in MP will be blocked.
	 *
	 * @param  i       Index of first item to receive, in the range 0 ..
	 *                 <TT>length</TT>-1.
	 * @param  num     Maximum number of items to receive.
	 * @param  buffer  Byte buffer.
	 *
	 * @return  Number of items received.
	 */
	protected int receiveItems
		(int i,
		 int num,
		 ByteBuffer buffer)
		{
		DoubleBuffer doublebuffer = buffer.asDoubleBuffer();
		int n = 0;
		int r = i / myColCount;
		int row = r * myRowStride + myLowerRow;
		int c = i % myColCount;
		int col = c * myColStride + myLowerCol;
		int ncols = Math.min (myColCount - c, doublebuffer.remaining());
		while (r < myRowCount && ncols > 0)
			{
			double[] myMatrix_row = myMatrix[row];
			while (c < ncols)
				{
				myMatrix_row[col] =
					myOp.op (myMatrix_row[col], doublebuffer.get());
				++ c;
				col += myColStride;
				}
			n += ncols;
			++ r;
			row += myRowStride;
			c = 0;
			col = myLowerCol;
			ncols = Math.min (myColCount, doublebuffer.remaining());
			}
		buffer.position (buffer.position() + 8*n);
		return n;
		}

	}