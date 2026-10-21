package com.xhzb.nursing.service.impl;

import java.io.InputStream;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.xhzb.common.utils.DateUtils;
import com.xhzb.oss.client.OSSAliyunFileStorageService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.KnowledgeBaseMapper;
import com.xhzb.nursing.domain.KnowledgeBase;
import com.xhzb.nursing.service.IKnowledgeBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Arrays;

/**
 * 知识库Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-10-21
 */
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements IKnowledgeBaseService
{
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;
    @Autowired
    private OSSAliyunFileStorageService oSSAliyunFileStorageService;

    @Autowired
    private VectorStore vectorStore;

    /**
     * 查询知识库
     * 
     * @param id 知识库主键
     * @return 知识库
     */
    @Override
    public KnowledgeBase selectKnowledgeBaseById(Integer id)
    {
        return getById(id);
    }

    /**
     * 查询知识库列表
     * 
     * @param knowledgeBase 知识库
     * @return 知识库
     */
    @Override
    public List<KnowledgeBase> selectKnowledgeBaseList(KnowledgeBase knowledgeBase)
    {
        return knowledgeBaseMapper.selectKnowledgeBaseList(knowledgeBase);
    }

    /**
     * 新增知识库
     * 
     * @param knowledgeBase 知识库
     * @return 结果
     */
    @Override
    public int insertKnowledgeBase(KnowledgeBase knowledgeBase)
    {
        InputStream inputStream = oSSAliyunFileStorageService.download(knowledgeBase.getDocumentUrl());
        InputStreamResource resource = new InputStreamResource(inputStream);

        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build()
                );

        List<Document> documents = reader.read();
        TextSplitter textSplitter = TokenTextSplitter.builder()
                .withChunkSize(500)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(10000)
                .withKeepSeparator(false)
                .build();

        List<Document> documentList = textSplitter.apply(documents);
        vectorStore.add(documentList);

        List<String> list = documentList.stream().map(Document::getId).toList();
        String jsonStr = JSONUtil.toJsonStr(list);

        knowledgeBase.setCreateTime(DateUtils.getNowDate());
        knowledgeBase.setRemark(jsonStr);
        boolean flag = save(knowledgeBase);

        return flag? 1 : 0;
    }

    /**
     * 修改知识库
     * 
     * @param knowledgeBase 知识库
     * @return 结果
     */
    @Override
    public int updateKnowledgeBase(KnowledgeBase knowledgeBase)
    {
        return updateById(knowledgeBase)? 1 : 0;
    }

    /**
     * 批量删除知识库
     * 
     * @param ids 需要删除的知识库主键
     * @return 结果
     */
    @Override
    public int deleteKnowledgeBaseByIds(Integer[] ids)
    {
        List<KnowledgeBase> knowledgeBaseList = this.listByIds(Arrays.asList(ids));
        if (CollUtil.isEmpty(knowledgeBaseList)){
            return 0;
        }
        for (KnowledgeBase knowledgeBase : knowledgeBaseList) {
            String documentUrl = knowledgeBase.getDocumentUrl();
            oSSAliyunFileStorageService.delete(documentUrl);

            String idsJson = knowledgeBase.getRemark();
            List<String> list = JSONUtil.toList(idsJson, String.class);
            vectorStore.delete(list);
        }

        boolean flag = this.removeByIds(Arrays.asList(ids));

        return flag ? 1 : 0;
    }

    /**
     * 删除知识库信息
     * 
     * @param id 知识库主键
     * @return 结果
     */
    @Override
    public int deleteKnowledgeBaseById(Integer id)
    {
        return removeById(id)? 1 : 0;
    }
}
