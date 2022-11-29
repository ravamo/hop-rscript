/*
 *   This file is part of PRCalcPlugin.
 *
 *   PRCalcPlugin is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PRCalcPlugin is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PPRCalcPlugin.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2013 dekarlab.de Theory. Solutions. Online Training.
 */

package red.clover.bi.plugin;

import red.clover.bi.PRVariable;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.pipeline.transform.BaseTransformData;
import org.apache.hop.pipeline.transform.ITransformData;

public class PRCalcData extends BaseTransformData implements ITransformData {



	/**
	 * New output row meta with new variables.
	 */
	private IRowMeta outputRowMeta;
	/**
	 * Initial count of columns.
	 */
	private int inputSize;

	private PRVariable[] inputVars;
	private PRVariable[] outputVars;

	public PRCalcData() {
		super();
	}

	public IRowMeta getOutputRowMeta() {
		return outputRowMeta;
	}

	public void setOutputRowMeta(IRowMeta outputRowMeta) {
		this.outputRowMeta = outputRowMeta;
	}

	public int getInputSize() {
		return inputSize;
	}

	public void setInputSize(int inputSize) {
		this.inputSize = inputSize;
	}

	public PRVariable[] getInputVars() {
		return inputVars;
	}

	public void setInputVars(PRVariable[] inputVars) {
		this.inputVars = inputVars;
	}

	public PRVariable[] getOutputVars() {
		return outputVars;
	}

	public void setOutputVars(PRVariable[] outputVars) {
		this.outputVars = outputVars;
	}

}
