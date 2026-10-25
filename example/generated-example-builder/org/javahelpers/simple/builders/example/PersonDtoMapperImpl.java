package org.javahelpers.simple.builders.example;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor"
)
public class PersonDtoMapperImpl implements PersonDtoMapper {

    @Override
    public PersonDto copy(PersonDto source) {
        if ( source == null ) {
            return null;
        }

        String name = null;

        name = source.getName();

        PersonDto personDto = new PersonDto( name );

        List<String> list = source.getNickNames();
        if ( list != null ) {
            personDto.setNickNames( new ArrayList<String>( list ) );
        }
        personDto.setBirthdate( source.getBirthdate() );
        personDto.setMannschaft( source.getMannschaft() );

        return personDto;
    }
}
