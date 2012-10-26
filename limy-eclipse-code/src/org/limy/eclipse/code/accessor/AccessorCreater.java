/*
 * Created 2004/11/15
 * Copyright (C) 2003-2009  Naoki Iwami (naoki@limy.org)
 *
 * This file is part of Limy Eclipse Plugin.
 *
 * Limy Eclipse Plugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Limy Eclipse Plugin is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Limy Eclipse Plugin.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.limy.eclipse.code.accessor;

import static org.limy.eclipse.code.LimyCodeConstants.INDENT_STR;
import static org.limy.eclipse.code.LimyCodeConstants.JAVADOC_CONT;
import static org.limy.eclipse.code.LimyCodeConstants.JAVADOC_END;
import static org.limy.eclipse.code.LimyCodeConstants.JAVADOC_START;
import static org.limy.eclipse.code.LimyCodeConstants.RIGHT_BRACKET;
import static org.limy.eclipse.common.LimyEclipseConstants.NL;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.env.ISourceField;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.corext.codemanipulation.GetterSetterUtil;
import org.limy.eclipse.code.LimyCodeConstants;
import org.limy.eclipse.code.LimyCodePlugin;

/**
 * ���\�b�h��`���𐶐����郆�[�e�B���e�B�N���X�ł��B
 * @author Naoki Iwami
 */
public final class AccessorCreater {
    
    // ------------------------ Constructors

    /**
     * private constructor
     */
    private AccessorCreater() { }

    // ------------------------ Public Methods

    /**
     * getter���\�b�h��`���iJavadoc�܁j�𐶐����܂��B
     * @param field �t�B�[���h�I�u�W�F�N�g
     * @param fieldComment �t�B�[���h�R�����g
     * @param fieldAnnotation �t�B�[���h�A�m�e�[�V�����q���g
     * @param flagName �A�N�Z�X���ʎq������
     * @param method ���\�b�h�I�u�W�F�N�g
     * @return getter���\�b�h��`��
     * @throws JavaModelException
     */
    public static String createGetterContents(
            IField field, String fieldComment, String fieldAnnotation,
            String flagName, IMethod method)
            throws JavaModelException {
        
        // �t�B�[���h�����擾
        ISourceField elementInfo =
            (ISourceField)((JavaElement)field).getElementInfo();
        // �t�B�[���h�^�����擾
        String typeName = new String(elementInfo.getTypeName());
        // �t�B�[���h�����擾
        String fieldName = field.getElementName();
        String getFieldName = GetterSetterUtil.getGetterName(field, null);
        
        String tmpFieldComment = fieldComment;
        if (fieldComment == null || fieldComment.length() == 0) {
            tmpFieldComment = fieldName;
        }
        // fk 2012.10.26 �t�B�[���h���x���␳�ǉ�
        if (tmpFieldComment.endsWith(".")) {
            tmpFieldComment = tmpFieldComment.substring(0,tmpFieldComment.length() - 1);
        }
        // fk 2012.10.26
        
        StringBuilder buff = new StringBuilder(256);
        appendJavadocGetter(buff, tmpFieldComment);
        
        if (method != null && method.exists()) {
            appendMethodSourceAlready(method, buff, flagName);
        } else {
            if (fieldAnnotation != null && fieldAnnotation.length() > 0) {
                buff.append(INDENT_STR).append(fieldAnnotation).append(NL);
            }
            buff.append(INDENT_STR).append(flagName).append(" ").append(typeName);
            buff.append(' ').append(getFieldName).append("() {").append(NL);
            buff.append("        return ").append(fieldName).append(';').append(NL);
            buff.append(INDENT_STR).append(RIGHT_BRACKET).append(NL).append(NL);
        }
        return buff.toString();
    }

    /**
     * setter���\�b�h��`���iJavadoc�܁j�𐶐����܂��B
     * @param field �t�B�[���h�I�u�W�F�N�g
     * @param fieldComment �t�B�[���h�R�����g
     * @param flagName �A�N�Z�X���ʎq������
     * @param method ���\�b�h�I�u�W�F�N�g
     * @return setter���\�b�h��`��
     * @throws JavaModelException
     */
    public static String createSetterContents(
            IField field, String fieldComment, String flagName, IMethod method)
            throws JavaModelException {
            
        ISourceField elementInfo =
            (ISourceField)((JavaElement)field).getElementInfo(); // �t�B�[���h�����擾
        String typeName = new String(elementInfo.getTypeName()); // �t�B�[���h�^�����擾
        String fieldName = field.getElementName(); // �t�B�[���h�����擾
        String myFieldName = fieldName;
        if (myFieldName.charAt(0) == '_') {
            myFieldName = myFieldName.substring(1);
        }
        if (method != null) {
            // �������\�b�h�̃R�����g�𐶐�����Ƃ��́A���\�b�h�����������g�p����
            myFieldName = method.getParameterNames()[0];
        }
        
        String setFieldName = GetterSetterUtil.getSetterName(field, null);

        String tmpFieldComment = fieldComment;
        if (fieldComment == null || fieldComment.length() == 0) {
            tmpFieldComment = myFieldName;
        }
        // fk 2012.10.26 �t�B�[���h���x���␳�ǉ�
        if (tmpFieldComment.endsWith(".")) {
            tmpFieldComment = tmpFieldComment.substring(0,tmpFieldComment.length() - 1);
        }
        // fk 2012.10.26

        StringBuilder buff = new StringBuilder(256);
        appendJavadocSetter(buff, tmpFieldComment, myFieldName);
        
        if (method != null && method.exists()) {
            appendMethodSourceAlready(method, buff, flagName);
        } else {
            buff.append(INDENT_STR).append(flagName).append(" void ");
            buff.append(setFieldName).append('(').append(typeName).append(' ');
            buff.append(myFieldName).append(") {").append(NL);
            buff.append("        this.").append(fieldName).append(" = ");
            buff.append(myFieldName).append(';').append(NL);
            buff.append(INDENT_STR).append(RIGHT_BRACKET).append(NL).append(NL);
        }

        return buff.toString();
    }
    
    // ------------------------ Private Methods

    /**
     * ������o�b�t�@��Getter���\�b�h��Javadoc�R�����g��ǉ����܂��B
     * @param buff ������o�b�t�@
     * @param fieldComment �t�B�[���h�R�����g
     */
    private static void appendJavadocGetter(StringBuilder buff,
            String fieldComment) {
        
        buff.append(INDENT_STR).append(JAVADOC_START).append(NL);
        buff.append(INDENT_STR).append(JAVADOC_CONT).append(fieldComment);
        buff.append(LimyCodePlugin.getDefault().getPreferenceStore().getString(
                LimyCodeConstants.PREF_GETTER_DESC)).append(NL);
        buff.append(INDENT_STR).append(" * @return ").append(fieldComment).append(NL);
        buff.append(INDENT_STR).append(JAVADOC_END).append(NL);
    }

    /**
     * ������o�b�t�@��Setter���\�b�h��Javadoc�R�����g��ǉ����܂��B
     * @param buff ������o�b�t�@
     * @param fieldComment �t�B�[���h�R�����g
     * @param fieldName �t�B�[���h���i���\�b�h�p�����[�^�̉��������j
     */
    private static void appendJavadocSetter(
            StringBuilder buff, String fieldComment, String fieldName) {
        buff.append(INDENT_STR).append(JAVADOC_START).append(NL);
        buff.append(INDENT_STR).append(JAVADOC_CONT).append(fieldComment);
        buff.append(LimyCodePlugin.getDefault().getPreferenceStore().getString(
                LimyCodeConstants.PREF_SETTER_DESC)).append(NL);
        buff.append(INDENT_STR).append(" * @param ").append(fieldName).append(' ');
        buff.append(fieldComment).append(NL);
        buff.append(INDENT_STR).append(JAVADOC_END).append(NL);
    }

    /**
     * �������\�b�h���烁�\�b�h��`���iJavadoc���������j�𐶐����܂��B
     * @param method ���\�b�h�I�u�W�F�N�g
     * @param buff �����o�b�t�@
     * @param flagName �A�N�Z�X���ʎq������
     * @throws JavaModelException
     */
    private static void appendMethodSourceAlready(
            IMethod method, StringBuilder buff, String flagName) throws JavaModelException {
        String source = method.getSource();
        String flagString = AccessorUtils.getFlagString(method.getFlags());
        if (source.startsWith(JAVADOC_START)) {
            // "/**"�Ŏn�܂��Ă���ꍇ
            
            // Javadoc�̏I�������o
            int index = source.indexOf("*/") + 2;
            while (Character.isWhitespace(source.charAt(index))) {
                ++index;
            }
            
            // �A�N�Z�X���ʎq������
            int startIndex = source.indexOf(flagString);

            // �A�N�Z�X���ʎq�ȑO��append
            String line = source.substring(index, startIndex);
            // �I�[�̃X�y�[�X�i���s�͊܂܂Ȃ��j��������append
            int lastPos = line.length() - 1;
            for (; lastPos >= 0; --lastPos) {
                char c = line.charAt(lastPos);
                if (c == 9 || c == ' ') {
                    continue;
                }
                break;
            }
            if (lastPos >= 0) {
                buff.append(INDENT_STR).append(line.substring(0, lastPos + 1));
            }
            
            // public �Ȃǂ̃A�N�Z�X���ʎq��append�i��������̂��̂ƕς��\��������̂Łj
            buff.append(INDENT_STR).append(flagName);
            
            // ���\�b�h��`����append
            buff.append(source.substring(startIndex + flagString.length())).append(NL).append(NL);
            
        } else {

            // �A�N�Z�X���ʎq������
            int startIndex = source.indexOf(flagString);
            // �A�N�Z�X���ʎq�ȑO��append
            buff.append(INDENT_STR).append(source.substring(0, startIndex));
            // �A�N�Z�X���ʎq��append
            buff.append(flagName);
            
            buff.append(source.substring(startIndex + flagString.length())).append(NL).append(NL);
        }
    }

}
