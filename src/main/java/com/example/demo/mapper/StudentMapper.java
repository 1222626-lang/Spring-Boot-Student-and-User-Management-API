
package com.example.demo.mapper;

import com.example.demo.dto.StudentRequest;
import com.example.demo.dto.StudentResponse;
import com.example.demo.entity.Student;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentMapper
        extends BaseMapper<Student, StudentRequest, StudentResponse> {
}
