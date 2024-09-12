//package br.com.swapi.util;
//
//import br.com.swapi.repository.FleetRepository;
//
//public class AvailabilityUtil {
//
//    private static final FleetRepository fleetRepository = new FleetRepository();
//
//    // Função centralizada de utilitário para checar a disponibilidade de um tripulante
//    public static boolean isCrewAvailable(int externalId) {
//        // Verificar no repositório se o tripulante já está alocado em alguma frota
//        return !fleetRepository.isCrewAllocated(externalId);
//    }
//}
