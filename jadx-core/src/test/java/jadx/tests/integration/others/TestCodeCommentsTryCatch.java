package jadx.tests.integration.others;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import jadx.api.data.ICodeComment;
import jadx.api.data.IJavaCodeRef;
import jadx.api.data.IJavaNodeRef.RefType;
import jadx.api.data.impl.JadxCodeComment;
import jadx.api.data.impl.JadxCodeData;
import jadx.api.data.impl.JadxCodeRef;
import jadx.api.data.impl.JadxNodeRef;
import jadx.tests.api.IntegrationTest;

import static jadx.tests.api.utils.assertj.JadxAssertions.assertThat;

public class TestCodeCommentsTryCatch extends IntegrationTest {

	public static class TestCls {
		public void test() {
			try {
				System.out.println("in try");
			} catch (Exception e) {
				System.out.println("in catch");
			}
		}
	}

	@Test
	public void test() {
		printOffsets();

		String baseClsId = TestCls.class.getName();
		JadxNodeRef mthRef = new JadxNodeRef(RefType.METHOD, baseClsId, "test()V");
		// Attach comment to the first instruction inside the try body.
		// In Dalvik the getstatic for System.out is at offset 0; in Java bytecode also 0.
		// After code-shrink the comment migrates to the invoke-virtual (println) which is
		// the first rendered instruction inside the try block.
		IJavaCodeRef insnRef = JadxCodeRef.forInsn(isJavaInput() ? 0 : 0);
		ICodeComment insnComment = new JadxCodeComment(mthRef, insnRef, "try body comment");

		JadxCodeData codeData = new JadxCodeData();
		codeData.setComments(Collections.singletonList(insnComment));
		getArgs().setCodeData(codeData);

		// Comment must appear exactly once – on the instruction line inside the try block,
		// NOT also on the "try {" header line (issue #2710).
		assertThat(getClassNode(TestCls.class))
				.decompile()
				.code()
				.containsOne("// try body comment");
	}
}
