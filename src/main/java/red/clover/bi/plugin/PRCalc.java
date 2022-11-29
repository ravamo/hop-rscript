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


import red.clover.bi.PRColumnVariable;
import red.clover.bi.PRPrintStream;
import red.clover.bi.PRVariable;
import red.clover.bi.RRunner;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.core.row.RowMeta;
import org.apache.hop.core.row.value.ValueMetaBase;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.TransformMeta;

/**
 *
 */
public class PRCalc extends BaseTransform<PRCalcMeta, PRCalcData> implements ITransform {

	private PRCalcMeta meta;
	private PRCalcData data;

	/**
	 *
	 * @param transformMeta
	 * @param prCalcMeta
	 * @param prCalcData
	 * @param copyNr
	 * @param pipelineMeta
	 * @param pipeline
	 */
	public PRCalc(
		final TransformMeta transformMeta,
		final PRCalcMeta prCalcMeta,
		final PRCalcData prCalcData,
		final int copyNr,
		PipelineMeta pipelineMeta,
		Pipeline pipeline
	) {
		super(transformMeta, prCalcMeta, prCalcData, copyNr, pipelineMeta, pipeline);
		data = prCalcData;
		meta = prCalcMeta;
	}

	/**
	 * Run R and put result to output.
	 * @param inputVars
	 * @param outputVars
	 * @throws Exception
	 */
	protected void runR(PRVariable[] inputVars, PRVariable[] outputVars)
			throws Exception {
		// Create R code runner.
		RRunner rr = RRunner.getInstance(
			resolve(meta.getScriptFilePath())
			, inputVars,
				outputVars);
		// Evaluate
		logBasic("Run R Script from " + meta.getScriptFilePath());
		rr.run();
		// Close
		rr.closeREngine();
		logBasic("R output: " + rr.getOutput());
		logBasic("R Script is evaluated. REngine is closed");
	}

	/**
	 * Initialize input and output variables for every row.
	 * 
	 * @param rowInput
	 * @param inputVars
	 * @param outputVars
	 */
	private void initInputOutputVars(Object[] rowInput, PRVariable[] inputVars,
			PRVariable[] outputVars) {
		// Create input variables with values.
		PRVariable varTemp;
		logDebug("Init Input Vars");
		for (int i = 0; i < meta.getInputVars().size(); i++) {
			varTemp = meta.getInputVars().get(i);
			if (meta.isInputTable()) {// Save all values in columns
				if (inputVars[i] == null) {// first row
					inputVars[i] = new PRColumnVariable(varTemp.getrName(),
							varTemp.getPentahoName(), varTemp.getType());
				}
			} else {
				inputVars[i] = new PRVariable(varTemp.getrName(),
						varTemp.getPentahoName(), varTemp.getType());
			}
			String[] fNames = getInputRowMeta().getFieldNames();
			for (int j = 0; j < fNames.length; j++) {
				if (fNames[j].equals(varTemp.getPentahoName())) {
					logDebug("Name: " + varTemp.getPentahoName() + " Class: "
							+ rowInput[j].getClass().getCanonicalName());
					inputVars[i].setValue(rowInput[j]);
					logDebug("Input: " + inputVars[i].getrName() + "="
							+ inputVars[i].getValue());
					break;
				}
			}
		}
		logDebug("Init Output Vars");
		// Create output variables.
		for (int i = 0; i < meta.getOutputVars().size(); i++) {
			varTemp = meta.getOutputVars().get(i);
			outputVars[i] = new PRVariable(varTemp.getrName(),
					varTemp.getPentahoName(), varTemp.getType());
		}
	}

	/**
	 * Initialize row meta.
	 */
	private void initOutputRowMeta() {
		System.setOut(new PRPrintStream(new ByteArrayOutputStream(), log));
		System.setErr(new PRPrintStream(new ByteArrayOutputStream(), log));
		IRowMeta outputRowMeta = null;
		if (meta.isInputTable() || getInputRowMeta() == null) {
			// output is only result
			outputRowMeta = new RowMeta();
		} else {
			// Add output variables as columns to result.
			outputRowMeta = getInputRowMeta().clone();
		}
		if (getInputRowMeta() != null) {
			data.setInputSize(getInputRowMeta().size());
		} else {
			data.setInputSize(0);
		}
		IValueMeta valueMeta;
		for (int i = 0; i < meta.getOutputVars().size(); i++) {
			valueMeta = new ValueMetaBase();
			valueMeta.setName(meta.getOutputVars().get(i).getPentahoName());
			valueMeta.setTrimType(convertRTypeToPentahoType(meta.getOutputVars()
					.get(i).getType()));
			outputRowMeta.addValueMeta(valueMeta);
		}
		data.setOutputRowMeta(outputRowMeta);
	}

	/**
	 * Load JRI library.
	 *
	 */
	protected void loadJRILibrary() throws Exception {
		logBasic("Loading JRI library from: "
				+ new File(System.getProperty("java.library.path"))
						.getCanonicalPath());
		System.setProperty("jri.ignore.ule", "yes");
		logBasic("R is installed in R_HOME: " + System.getenv("R_HOME"));
		// System.load("/home/dk/R/x86_64-pc-linux-gnu-library/2.15/rJava/jri/libjri.so");
		System.loadLibrary("jri");
		logBasic("JRI library is found!");
	}

	/**
	 * Process row.
	 */
	public final boolean processRow() throws HopException {
		// Get Input row
		Object[] rowInput = getRow();
		try {
			// load JRI library
			loadJRILibrary();
			// Initialize output row meta
			if (first) {
				first = false;
				initOutputRowMeta();
				// Initialize lists of input and output variables with values.
				data.setInputVars(new PRVariable[meta.getInputVars().size()]);
				data.setOutputVars(new PRVariable[meta.getOutputVars().size()]);
			}
			PRVariable[] inputVars = data.getInputVars();
			PRVariable[] outputVars = data.getOutputVars();
			if (rowInput == null) { // no more input to be expected...
				// this.logDebug("Input Table: " + meta.isInputTable());
				// this.logDebug("Var: " + inputVars[0]);
				if (meta.isInputTable() && inputVars[0] != null) {
					this.logDebug("Run R for Table.");
					// Run R for whole table
					runR(inputVars, outputVars);
					Object[] rowOutput = new Object[outputVars.length];
					// Add calculated outputs to output
					for (int i = 0; i < outputVars.length; i++) {
						rowOutput[i] = outputVars[i].getValue();
						logDebug("Result: " + outputVars[i].getrName() + "="
								+ outputVars[i].getValue());
					}
					putRow(data.getOutputRowMeta(), rowOutput);
				}
				this.logDebug("No More Rows.");
				setOutputDone();
				return false;
			}
			if (meta.isInputTable()) {
				// Save new values in variables
				initInputOutputVars(rowInput, inputVars, outputVars);
				return true;
			} else {
				// Return also values from input
				Object[] rowOutput = RowDataUtil.resizeArray(rowInput, data
						.getOutputRowMeta().size());
				// Initialize variables
				initInputOutputVars(rowInput, inputVars, outputVars);
				// Run R for every row.
				runR(inputVars, outputVars);
				// Add calculated outputs to output
				for (int i = 0; i < outputVars.length; i++) {
					rowOutput[data.getInputSize() + i] = outputVars[i]
							.getValue();
					logDebug("Result: " + outputVars[i].getrName() + "="
							+ outputVars[i].getValue());
				}
				putRow(data.getOutputRowMeta(), rowOutput);
				return true;
			}
		} catch (Exception e) {
			throw new HopException("Error by executing R script: "
					+ Arrays.toString(rowInput), e);
		}
	}

	/**
	 * Convert R Type to pentaho type.
	 * 
	 * @param rtype
	 * @return
	 */
	public static int convertRTypeToPentahoType(int rtype) {
		switch (rtype) {
		case PRVariable.TYPE_BOOLEAN:
			return IValueMeta.TYPE_BOOLEAN;
		case PRVariable.TYPE_NUMBER:
			return IValueMeta.TYPE_NUMBER;
		case PRVariable.TYPE_STRING:
			return IValueMeta.TYPE_STRING;
		}
		return IValueMeta.TYPE_STRING;
	}

	/**
	 * Initialize step.
	 */
	public final boolean init() {
		if (super.init()) {
			try {
				// this.logDebug("Meta Fields: " + meta.getFields().size());
				return true;
			} catch (Exception e) {
				logError("An error occurred, processing will be stopped: "
						+ e.getMessage());
				setErrors(1);
				stopAll();
			}
		}
		return false;
	}

	/**
	 * Dispose step.
	 */
	public void dispose() {
		super.dispose();
	}
}
