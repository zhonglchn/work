package com.leyou.item.service;

import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.entity.SpecGroup;
import com.leyou.item.entity.SpecParam;
import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SpecService {
    
    @Autowired
    private SpecGroupMapper groupMapper;
    
    @Autowired
    private SpecParamMapper paramMapper;


    public List<SpecGroup> findSpecGroupByCid(Long id) {
        SpecGroup record = new SpecGroup();
        record.setCid(id);
        List<SpecGroup> specGroups = groupMapper.select(record);
        if(CollectionUtils.isEmpty(specGroups)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return specGroups;
    }

    public List<SpecParam> findSpecParams(Long gid, Long cid, Boolean searching) {
        //注意：此时处理器中传下来的这三个参数都可能为null，但是实际开发中gid和cid必须有一个有值
        if(gid==null && cid==null){
            //如果gid和cid都是null，结果是没有用的，所以抛出异常。具体业务前面已经说明了。
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        //下面的写法绝对不会出现这种sql：select * from param where gid=xx and cid=null，因为通用mapper中的select方法本来就只会拼接非null值的条件
        SpecParam record = new SpecParam();
        record.setGroupId(gid);
        record.setCid(cid);
        record.setSearching(searching);//如果searching为true，存入数据库自动为1，searching为false，存入数据库自动为0
        List<SpecParam> list = paramMapper.select(record);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return list;
    }

    public List<SpecGroupDTO> findSpecGroupDtoByCid(Long id) {
        // 根据分类id查询规格组列表
        List<SpecGroup> groups = findSpecGroupByCid(id);
        // 将规格组集合转成规格组dto集合
        List<SpecGroupDTO> groupDTOS = BeanHelper.copyWithCollection(groups, SpecGroupDTO.class);


        //方案三    马上升值加新版
        //流时编程法
        //根据分类id将所有规格参数集合查询出来，然后在分别放入到规格组中去
        List<SpecParam> specParams = findSpecParams(null, id, null);
        //采用流的方式对规格参数集合按照规格组的id进行java端分组，注意不是在数据库端
        Map<Long, List<SpecParam>> paramMap = specParams.stream().collect(Collectors.groupingBy(SpecParam::getGroupId));
        //遍历规格组集合
        groupDTOS.forEach(groupDTO->{
            groupDTO.setParams(paramMap.get(groupDTO.getId()));
        });

        //方案二   领导很开心版
        //根据分类id将所有规格参数集合查询出来，然后在分别放入到规格组中去
//        List<SpecParam> specParams = findSpecParams(null, id, null);
//        groupDTOS.forEach(groupDTO->{
//            specParams.forEach(specParam -> {
//                //判断当前规格参数是否属于当前规格组
//                if(specParam.getGroupId().equals(groupDTO.getId())){
//                    //如果当前规格参数属于当前规格组，就添加到当前规格组中去
//                    groupDTO.getParams().add(specParam);
//                }
//            });
//        });

        //方案一   领导很郁闷版
//        groupDTOS.forEach(groupDTO->{
//            //根据规格组id查询规格参数集合
//            List<SpecParam> specParams = findSpecParams(groupDTO.getId(), null, null);
//            groupDTO.setParams(specParams);
//        });
        return groupDTOS;
    }
}