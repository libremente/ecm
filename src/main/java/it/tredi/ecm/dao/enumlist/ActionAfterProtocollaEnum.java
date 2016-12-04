package it.tredi.ecm.dao.enumlist;

import lombok.Getter;

@Getter
public enum ActionAfterProtocollaEnum {
	ESEGUI_TASK (1, "Esegui task");

	private int id;
	private String nome;

	private ActionAfterProtocollaEnum(int id, String nome){
		this.id = id;
		this.nome = nome;
	}

}