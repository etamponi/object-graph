package com.objectgraph.jobsystem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import javassist.CannotCompileException;
import javassist.CtMethod;
import javassist.bytecode.MethodInfo;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

class JobInfo {
	
	private final Map<Integer, CycleInfo> cycles;
	private final Map<Integer, JobInfo> innerJobs;
	
	private final LinkedList<CycleInfo> currentCycles;
	
	private int position, codeLength;
	private int runningLength;
	
	private int callPosition;
	
	private final CtMethod method;
	private final String className, jobName;
	
	public JobInfo(CtMethod method, String className, String jobName) {
		this.cycles = new HashMap<>();
		this.innerJobs = new HashMap<>();
		this.currentCycles = null;
		this.className = className;
		this.jobName = jobName;
		this.method = method;
		try {
			MethodInfo info = method.getMethodInfo();
			this.position = info.getLineNumber(0);
			int length = info.getLineNumber(info.getCodeAttribute().getCodeLength()) - position;
			if (length > 0)
				codeLength = length - 1; // Remove final }
			else
				codeLength = 0;

			final Stack<CycleInfo> stack = new Stack<>();
			
			method.instrument(new ExprEditor() {
				@Override public void edit(MethodCall m) throws CannotCompileException {
					if (m.getMethodName().equals("startCycle")) {
						CycleInfo info = new CycleInfo(m.getLineNumber());
						stack.push(info);
					}
					if (m.getMethodName().equals("endCycle")) {
						CycleInfo info = stack.pop();
						info.setCodeLength(m.getLineNumber() - info.getCodePosition());
						info.setRunningLength(info.getCodeLength());
						cycles.put(info.getCodePosition(), info);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private JobInfo(JobInfo other, int callPosition) {
		this.cycles = other.cycles;
		this.className = other.className;
		this.codeLength = other.codeLength;
		this.innerJobs = other.innerJobs;
		this.position = other.position;
		this.runningLength = other.runningLength;
		this.jobName = other.jobName;
		this.method = other.method;
		
		this.currentCycles = new LinkedList<>();
		this.callPosition = callPosition;
	}

	public Map<Integer, CycleInfo> getCycles() {
		return cycles;
	}

	public LinkedList<CycleInfo> getCurrentCycles() {
		return currentCycles;
	}

	public int getPosition() {
		return position;
	}

	public int getCodeLength() {
		return codeLength;
	}
	
	public int getRunningLength() {
		return runningLength;
	}
	
	public void computeRunningLength(final Map<String, JobInfo> jobs) {
		runningLength = codeLength;
		try {
			method.instrument(new ExprEditor() {
				@Override public void edit(MethodCall m) throws CannotCompileException {
					String name = m.getMethodName();
					if (jobs.containsKey(name)) {
						runningLength += jobs.get(name).getCodeLength();
						innerJobs.put(m.getLineNumber(), jobs.get(name));
					}
				}
			});
			
			for(CycleInfo cycle: cycles.values()) {
				cycle.setRunningPosition(getRunningLine(cycle.getCodePosition()));
				int runningLength = cycle.getCodeLength();
				for(int jobLine: innerJobs.keySet()) {
					if (jobLine >= cycle.getCodePosition() && jobLine <= (cycle.getCodePosition()+cycle.getCodeLength()))
						runningLength += innerJobs.get(jobLine).getCodeLength();
				}
				cycle.setRunningLength(runningLength);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public int getRunningLine(int line) {
		int ret = line;
		for(int jobLine: innerJobs.keySet()) {
			if (jobLine < line) {
				ret += innerJobs.get(jobLine).getCodeLength();
			}
		}
		return ret;
	}

	public JobInfo instantiate(int callPosition) {
		return new JobInfo(this, callPosition);
	}

	public String getJobName() {
		return jobName;
	}

	public String getTypeName() {
		return className;
	}
	
	public int getCallPosition() {
		return callPosition;
	}
	
}
