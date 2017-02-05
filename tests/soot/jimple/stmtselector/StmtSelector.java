package soot.jimple.stmtselector;

import java.util.Set;

import soot.Unit;

public interface StmtSelector {
	Set<Unit> nthStmt(int n);
	Set<Unit> nthMergePoint(int n);
	Set<Unit> lastStmt();
	Set<Unit> firstStmt();
	Set<Unit> anyCallTo(String method);
}
