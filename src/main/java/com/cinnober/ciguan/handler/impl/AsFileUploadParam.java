/*
 * 
 *
 * 
 * 
 *
 * This software is the confidential and proprietary information of
 * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Cinnober.
 *
 * Cinnober makes no representations or warranties about the suitability
 * of the software, either expressed or implied, including, but not limited
 * to, the implied warranties of merchantibility, fitness for a particular
 * purpose, or non-infringement. Cinnober shall not be liable for any
 * damages suffered by licensee as a result of using, modifying, or
 * distributing this software or its derivatives.
 */
package com.cinnober.ciguan.handler.impl;

import org.apache.commons.fileupload.FileItem;

/**
 * Holding file upload data.
 */
public class AsFileUploadParam {
    
    /** The item. */
    private FileItem mItem;
    
    /** The upload handler. */
    private String mUploadHandler;
    
    /**
     * Instantiates a new as file upload param.
     *
     * @param pItem the item
     */
    public AsFileUploadParam(FileItem pItem) {
        mItem = pItem;
    }

    /**
     * Instantiates a new as file upload param.
     *
     * @param pUploadHandler the upload handler
     */
    public AsFileUploadParam(String pUploadHandler) {
        mUploadHandler = pUploadHandler;
    }

    /**
     * Sets the file item.
     *
     * @param pFileItem the new file item
     */
    public void setFileItem(FileItem pFileItem) {
    }
    
    /**
     * Gets the file item.
     *
     * @return the file item
     */
    public FileItem getFileItem() {
        return mItem;
    }
    
    /**
     * Sets the upload handler.
     *
     * @param pUploadHandler the new upload handler
     */
    public void setUploadHandler(String pUploadHandler) {
        mUploadHandler = pUploadHandler;
    }
    
    /**
     * Gets the upload handler.
     *
     * @return the upload handler
     */
    public String getUploadHandler() {
        return mUploadHandler;
    }
    
    @Override
    public String toString() {
        StringBuilder tBuilder = new StringBuilder();
        if (mItem != null) {
            if (mItem.isFormField()) {
                tBuilder.append("fieldName=").append(mItem.getFieldName());
                tBuilder.append("; fieldValue=").append(mItem.getString());
            }
            else {
                tBuilder.append("fieldName=").append(mItem.getFieldName());
                tBuilder.append("; fileName=").append(mItem.getName());
                tBuilder.append("; contentType=").append(mItem.getContentType());
                tBuilder.append("; inMemory=").append(mItem.isInMemory());
                tBuilder.append("; size=").append(mItem.getSize());
                tBuilder.append("; handler=").append(mUploadHandler);
            }
        }
        return tBuilder.toString();
    }

}
