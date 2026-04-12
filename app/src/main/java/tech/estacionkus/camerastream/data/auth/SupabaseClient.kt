package tech.estacionkus.camerastream.data.auth

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object Supabase {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://woqkueabensezxjvlzjr.supabase.co",
        supabaseKey = "sb_publishable_dQeAB8M2FuR0zdweik9Ttw_jVaOahwj"
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
    }
}
